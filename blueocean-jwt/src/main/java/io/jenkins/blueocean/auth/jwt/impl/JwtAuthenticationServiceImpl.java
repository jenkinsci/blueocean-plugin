package io.jenkins.blueocean.auth.jwt.impl;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.User;
import hudson.tasks.Mailer;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationService;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationStore;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationStoreFactory;
import io.jenkins.blueocean.auth.jwt.JwtToken;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

/**
 * Default implementation of {@link JwtAuthenticationService}
 *
 * @author Vivek Pandey
 */
@Extension
public class JwtAuthenticationServiceImpl extends JwtAuthenticationService {

    private static int DEFAULT_EXPIRY_IN_SEC = 1800;
    private static int DEFAULT_MAX_EXPIRY_TIME_IN_MIN = 480;
    private static int DEFAULT_NOT_BEFORE_IN_SEC = 30;

    @Override
    public JwtToken getToken(@Nullable @QueryParameter("expiryTimeInMins") Integer expiryTimeInMins, @Nullable @QueryParameter("maxExpiryTimeInMins") Integer maxExpiryTimeInMins) {
        long expiryTime= Long.getLong("EXPIRY_TIME_IN_MINS",DEFAULT_EXPIRY_IN_SEC);

        int maxExpiryTime = Integer.getInteger("MAX_EXPIRY_TIME_IN_MINS",DEFAULT_MAX_EXPIRY_TIME_IN_MIN);

        if(maxExpiryTimeInMins != null){
            maxExpiryTime = maxExpiryTimeInMins;
        }
        if(expiryTimeInMins != null){
            if(expiryTimeInMins > maxExpiryTime) {
                throw new ServiceException.BadRequestException(
                    String.format("expiryTimeInMins %s can't be greater than %s", expiryTimeInMins, maxExpiryTime));
            }
            expiryTime = expiryTimeInMins * 60;
        }

        Authentication authentication = Jenkins.getAuthentication();

        String userId = authentication.getName();

        User user = User.get(userId, false, Collections.emptyMap());
        String email = null;
        String fullName = null;
        if(user != null) {
            fullName = user.getFullName();
            userId = user.getId();
            Mailer.UserProperty p = user.getProperty(Mailer.UserProperty.class);
            if(p!=null)
                email = p.getAddress();
        }
        Plugin plugin = Jenkins.getInstance().getPlugin("blueocean-jwt");
        String issuer = "blueocean-jwt:"+ ((plugin!=null) ? plugin.getWrapper().getVersion() : "");

        JwtToken jwtToken = new JwtToken();
        jwtToken.claim.put("jti", UUID.randomUUID().toString().replace("-",""));
        jwtToken.claim.put("iss", issuer);
        jwtToken.claim.put("sub", userId);
        jwtToken.claim.put("name", fullName);
        long currentTime = System.currentTimeMillis()/1000;
        jwtToken.claim.put("iat", currentTime);
        jwtToken.claim.put("exp", currentTime+expiryTime);
        jwtToken.claim.put("nbf", currentTime - DEFAULT_NOT_BEFORE_IN_SEC);

        //set claim
        JSONObject context = new JSONObject();

        JSONObject userObject = new JSONObject();
        userObject.put("id", userId);
        userObject.put("fullName", fullName);
        userObject.put("email", email);

        JwtAuthenticationStore authenticationStore = getJwtStore(authentication);

        authenticationStore.store(authentication, context);

        context.put("user", userObject);
        jwtToken.claim.put("context", context);

        return jwtToken;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "BlueOcean Jwt endpoint";
    }

    public static JwtAuthenticationStore getJwtStore(Authentication authentication){
        JwtAuthenticationStore jwtAuthenticationStore=null;
        for(JwtAuthenticationStoreFactory factory: JwtAuthenticationStoreFactory.all()){
            if(factory instanceof SimpleJwtAuthenticationStore){
                jwtAuthenticationStore = factory.getJwtAuthenticationStore(authentication);
                continue;
            }
            JwtAuthenticationStore authenticationStore = factory.getJwtAuthenticationStore(authentication);
            if(authenticationStore != null){
                return authenticationStore;
            }
        }

        //none found, lets use SimpleJwtAuthenticationStore
        return jwtAuthenticationStore;
    }
}


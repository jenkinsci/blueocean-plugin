package io.jenkins.blueocean.service.embedded;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.Permission;
import io.jenkins.blueocean.BlueOceanUI;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class BlueOceanRootAction implements UnprotectedRootAction, StaplerProxy {
    private static final String URL_BASE="blue";
    private static final Long randomBits = new Random().nextLong();

    private final boolean enableJWT = BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION;

    @Inject
    private BlueOceanUI app;

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * This would map to /jenkins/blue/
     */
    @Override
    public String getUrlName() {
        return URL_BASE;
    }

    @Override
    public Object getTarget() {

        StaplerRequest request = Stapler.getCurrentRequest();

        if(request.getOriginalRestOfPath().startsWith("/rest/")) {
            if(enableJWT) {
                Authentication tokenAuthentication = JwtAuthenticationToken.create(request);

                //create a new context and set it to holder to not clobber existing context
                SecurityContext securityContext = new SecurityContextImpl();
                securityContext.setAuthentication(tokenAuthentication);
                SecurityContextHolder.setContext(securityContext);

                //TODO: implement this as filter, see PluginServletFilter to clear the context
            } else {
                // Add X-Blueocean-Refresher to response, so that we can detect when the user changes. Wont be needed with JWT
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    throw new ServiceException.UnexpectedErrorException("Error getting sha1 algorithm", e);
                }
                String blueoceanDownGradeheader = Jenkins.getAuthentication().getName() + Long.toString(randomBits);

                md.update(blueoceanDownGradeheader.getBytes());

                Stapler.getCurrentResponse().setHeader("X-Blueocean-Refresher", DatatypeConverter.printHexBinary(md.digest()));

            }
        }else{
            //If user doesn't have overall Jenkins read permission then return 403, which results in classic UI redirecting
            // user to login page
                     Jenkins.getInstance().checkPermission(Permission.READ);
        }

        return app;
    }

    /** Provides implementation of BlueOceanUI */
    @Extension
    public static class ModuleImpl implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bind(BlueOceanUI.class).toInstance(new BlueOceanUI(URL_BASE));
        }
    }
}

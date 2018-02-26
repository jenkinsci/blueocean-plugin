package io.jenkins.blueocean.auth.jwt.impl;

import hudson.Extension;
import hudson.model.User;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationStore;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationStoreFactory;
import io.jenkins.blueocean.auth.jwt.JwtSigningKeyProvider;
import io.jenkins.blueocean.auth.jwt.JwtTokenVerifier;
import io.jenkins.blueocean.auth.jwt.SigningPublicKey;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.jose4j.jws.AlgorithmIdentifiers.RSA_USING_SHA256;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension(ordinal = -9999)
public class JwtTokenVerifierImpl extends JwtTokenVerifier {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenVerifierImpl.class);

    @Override
    public Authentication verify(HttpServletRequest request) {
        return  validate(request);
    }

    /**
     * @return
     *      null if the JWT token is not present
     * @throws Exception
     *      if the JWT token is present but invalid
     */
    private @CheckForNull Authentication validate(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return null;
        }
        String token = authHeader.substring("Bearer ".length());
        JsonWebStructure jws = parse(token);
        if (jws==null) {
            return null;
        }
        try {
            String alg = jws.getAlgorithmHeaderValue();
            if(alg == null || !alg.equals(RSA_USING_SHA256)){
                logger.error(String.format("Invalid JWT token: unsupported algorithm in header, found %s, expected %s", alg, RSA_USING_SHA256));
                throw new ServiceException.UnauthorizedException("Invalid JWT token");
            }

            String kid = jws.getKeyIdHeaderValue();

            if(kid == null){
                logger.error("Invalid JWT token: missing kid");
                throw new ServiceException.UnauthorizedException("Invalid JWT token");
            }

            SigningPublicKey publicKey = JwtSigningKeyProvider.toPublicKey(kid);
            if(publicKey == null){
                throw new ServiceException.UnexpectedErrorException("Invalid kid="+kid);
            }

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime() // the JWT must have an expiration time
                .setRequireJwtId()
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setRequireSubject() // the JWT must have a subject claim
                .setVerificationKey(publicKey.getKey()) // verify the sign with the public key
                .build(); // create the JwtConsumer instance

            try {
                JwtContext context = jwtConsumer.process(token);
                JwtClaims claims = context.getJwtClaims();

                String subject = claims.getSubject();
                if(subject.equals("anonymous")) { //if anonymous, we do not bother checking expiration
                    return Jenkins.ANONYMOUS;
                }else{
                    // If not anonymous user, get Authentication object associated with this claim
                    // We give a change to the authentication store to inspect the claims and if expired it might
                    // do cleanup of associated Authentication object for example.
                    JwtAuthenticationStore authenticationStore = getJwtStore(claims.getClaimsMap());
                    Authentication authentication = authenticationStore.getAuthentication(claims.getClaimsMap());

                    // Now check if token expired
                    NumericDate expirationTime = claims.getExpirationTime();
                    if (expirationTime.isBefore(NumericDate.now())){
                        throw new ServiceException.UnauthorizedException("Invalid JWT token: expired");

                    }
                    return authentication;
                }


            } catch (InvalidJwtException e) {
                logger.error("Invalid JWT token: "+e.getMessage(), e);
                throw new ServiceException.UnauthorizedException("Invalid JWT token");
            } catch (MalformedClaimException e) {
                logger.error(String.format("Error reading sub header for token %s",jws.getPayload()),e);
                throw new ServiceException.UnauthorizedException("Invalid JWT token: malformed claim");
            }
        } catch (JoseException e) {
            logger.error("Error parsing JWT token: "+e.getMessage(), e);
            throw new ServiceException.UnauthorizedException("Invalid JWT Token: "+ e.getMessage());
        }
    }

    private JsonWebStructure parse(String token) {
        try {
            return JsonWebStructure.fromCompactSerialization(token);
        } catch (JoseException e) {
            // token was not formed as JWT token. Probably it's a different kind of bearer token
            // some other plugins have introduced
            return null;
        }
    }

    private static JwtAuthenticationStore getJwtStore(Map<String,Object> claims){
        JwtAuthenticationStore jwtAuthenticationStore=null;
        for(JwtAuthenticationStoreFactory factory: JwtAuthenticationStoreFactory.all()){
            if(factory instanceof SimpleJwtAuthenticationStore){
                jwtAuthenticationStore = factory.getJwtAuthenticationStore(claims);
                continue;
            }
            JwtAuthenticationStore authenticationStore = factory.getJwtAuthenticationStore(claims);
            if(authenticationStore != null){
                return authenticationStore;
            }
        }

        //none found, lets use SimpleJwtAuthenticationStore
        return jwtAuthenticationStore;
    }

    public static class JwtAuthentication extends AbstractAuthenticationToken{
        private final String name;
        private final GrantedAuthority[] grantedAuthorities;

        public JwtAuthentication(String subject) {
            User user = User.get(subject, false, Collections.emptyMap());
            if (user == null) {
                throw new ServiceException.UnauthorizedException("Invalid JWT token: subject " + subject + " not found");
            }
            //TODO: UserDetails call is expensive, encode it in token and create UserDetails from it
            UserDetails d = Jenkins.getInstance().getSecurityRealm().loadUserByUsername(user.getId());
            this.grantedAuthorities = d.getAuthorities();
            this.name = subject;
            super.setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return "";
        }

        @Override
        public Object getPrincipal() {
            return name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public GrantedAuthority[] getAuthorities() {
            //Fix for FB warning: EI_EXPOSE_REP
            return Arrays.copyOf(grantedAuthorities, grantedAuthorities.length);
        }
    }
}

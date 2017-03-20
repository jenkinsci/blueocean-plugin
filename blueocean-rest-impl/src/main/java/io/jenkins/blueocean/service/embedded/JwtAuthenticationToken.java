package io.jenkins.blueocean.service.embedded;

import hudson.model.User;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationStore;
import io.jenkins.blueocean.auth.jwt.JwtAuthenticationStoreFactory;
import io.jenkins.blueocean.auth.jwt.JwtToken;
import io.jenkins.blueocean.auth.jwt.impl.SimpleJwtAuthenticationStore;
import io.jenkins.blueocean.commons.ServiceException;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.lang.JoseException;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.jose4j.jws.AlgorithmIdentifiers.RSA_USING_SHA256;

/**
 * @author Kohsuke Kawaguchi
 */
public final class JwtAuthenticationToken{

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationToken.class);

    public static Authentication create(StaplerRequest request){
        JwtClaims claims = validate(request);
        try {
            String subject = claims.getSubject();

            if(subject.equals("anonymous")) { //if anonymous, we don't look in user db
                return Jenkins.getInstance().ANONYMOUS;
            }else{

                JwtAuthenticationStore authenticationStore = getJwtStore(claims.getClaimsMap());
                Authentication authentication = authenticationStore.getAuthentication(claims.getClaimsMap());
                if(authentication == null){
                    throw new ServiceException.UnauthorizedException("Unauthorized: No valid authentication instance found");
                }
                return authentication;
            }
        } catch (MalformedClaimException e) {
            logger.error(String.format("Error reading sub header for token %s",claims.getRawJson()),e);
            throw new ServiceException.UnauthorizedException("Invalid JWT token: malformed claim");
        }
    }


    public JwtAuthenticationToken(JwtClaims claims) throws MalformedClaimException {
        String subject = claims.getSubject();
        User user = User.get(subject, false, Collections.emptyMap());
        if (user == null) {
            throw new ServiceException.UnauthorizedException("Invalid JWT token: subject " + subject + " not found");
        }
    }

    private  static JwtClaims validate(StaplerRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new ServiceException.UnauthorizedException("JWT token not found");
        }
        String token = authHeader.substring("Bearer ".length());
        try {
            JsonWebStructure jws = JsonWebStructure.fromCompactSerialization(token);
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

            JwtToken.JwtRsaDigitalSignatureKey key = new JwtToken.JwtRsaDigitalSignatureKey(kid);
            try {
                if(!key.exists()){
                    throw new ServiceException.NotFoundException(String.format("kid %s not found", kid));
                }
            } catch (IOException e) {
                logger.error(String.format("Error reading RSA key for id %s: %s",kid,e.getMessage()),e);
                throw new ServiceException.UnexpectedErrorException("Unexpected error: "+e.getMessage(), e);
            }

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime() // the JWT must have an expiration time
                .setRequireJwtId()
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setRequireSubject() // the JWT must have a subject claim
                .setVerificationKey(key.getPublicKey()) // verify the sign with the public key
                .build(); // create the JwtConsumer instance

            try {
                JwtContext context = jwtConsumer.process(token);
                JwtClaims claims = context.getJwtClaims();

                //check if token expired
                NumericDate expirationTime = claims.getExpirationTime();
                if (expirationTime.isBefore(NumericDate.now())){
                    throw new ServiceException.UnauthorizedException("Invalid JWT token: expired");
                }

                return claims;
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

}

package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionPoint;
import hudson.model.RootAction;
import io.jenkins.blueocean.auth.jwt.impl.JwtTokenImpl;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

import javax.annotation.Nullable;

/**
 * JWT endpoint resource. Provides functionality to get JWT token and also provides JWK endpoint to get
 * public key using keyId.
 *
 * @author Vivek Pandey
 */
public abstract class JwtAuthenticationService implements RootAction, ExtensionPoint{

    @Override
    public String getUrlName() {
        return "jwt-auth";
    }


    /**
     * Gives JWT token for authenticated user. See https://tools.ietf.org/html/rfc7519.
     *
     * @param expiryTimeInMins token expiry time. Default 30 min.
     * @param maxExpiryTimeInMins max token expiry time. Default expiry time is 8 hours (480 mins)
     *
     * @return JWT if there is authenticated user or if  anonymous user has at least READ permission, otherwise 401
     *         error code is returned
     *
     *  @see JwtTokenImpl
     */
    @GET
    @WebMethod(name = "token")
    public abstract JwtToken getToken(@Nullable @QueryParameter("expiryTimeInMins") Integer expiryTimeInMins,
                                          @Nullable  @QueryParameter("maxExpiryTimeInMins") Integer maxExpiryTimeInMins);

    /**
     *  Gives Json web key. See https://tools.ietf.org/html/rfc7517
     *
     * @param keyId keyId of the key
     *
     * @return JWK reponse
     */
    @GET
    public abstract JwkService getJwks(String keyId);
}

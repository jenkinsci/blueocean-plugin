package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionPoint;
import hudson.model.UnprotectedRootAction;
import net.sf.json.JSONObject;
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
public abstract class JwtAuthenticationService implements UnprotectedRootAction, ExtensionPoint{

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
     */
    @GET
    @WebMethod(name = "token")
    public abstract JwtToken getToken(@Nullable @QueryParameter("expiryTimeInMins") Integer expiryTimeInMins,
                                          @Nullable  @QueryParameter("maxExpiryTimeInMins") Integer maxExpiryTimeInMins);

    /**
     * Binds Json web key to the URL space.
     *
     * @param keyId keyId of the key
     *
     * @return JWK response
     * @see <a href="https://tools.ietf.org/html/rfc7517">the spec</a>
     */
    @GET
    public SigningPublicKey getJwks(String keyId) {
        return JwtSigningKeyProvider.toPublicKey(keyId);
    }

    /**
     * Binds Json web keys to the URL space.
     *
     * @return a JWKS
     * @see <a href="https://tools.ietf.org/html/rfc7517#page-10">the JWK Set Format spec</a>
     */
    @GET
    @WebMethod(name = "jwk-set") // we could not name this endpoint /jwks as it would be shadowing the pre-existing one
    public abstract JSONObject getJwkSet();
}

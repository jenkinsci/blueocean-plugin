package io.jenkins.blueocean.auth.jwt;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.WebMethod;

/**
 * Issuer of JSON Web Key.
 *
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 * @see JwtAuthenticationService#getJwks(String)
 */
public abstract class JwkService {

    /**
     *
     * @return Gives JWK JSONObject
     */
    @WebMethod(name = "")
    public abstract JSONObject getJwk();
}

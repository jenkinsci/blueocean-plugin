package io.jenkins.blueocean.auth.jwt;

import org.acegisecurity.Authentication;

import java.util.Map;

/**
 * An authentication store for Jwt, authentication provider implements this extension point to store enough information
 * in JWT claim so that later on when the token verification happens, using this same claims this authentication object
 * can be re-created.
 *
 * @author Vivek Pandey
 *
 * @see Authentication
 */
public interface JwtAuthenticationStore{

    /**
     * Given JWT claim give the authentication object
     *
     * @param claims JWT claim
     *
     * @return Authentication object, always non-null
     */
    Authentication getAuthentication(Map<String,Object> claims);


    /**
     * Store authentication related information in JWT claims
     *
     * @param claims JWT claim
     */
    void store(Authentication authentication, Map<String,Object> claims);
}

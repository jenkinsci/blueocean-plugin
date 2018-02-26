package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import org.acegisecurity.Authentication;

import java.util.Map;

/**
 * An authentication provider implements this extension point to store enough information in JWT claim so that later on
 * when the token verification happens, using this same claims this authentication object can be re-created.
 *
 * @author Vivek Pandey
 *
 * @see Authentication
 */
public abstract class JwtAuthenticationStoreFactory implements ExtensionPoint{

    /**
     * Resolves {@link JwtAuthenticationStore} for given {@link Authentication} instance.
     *
     * @param claims JWT claims
     *
     * @return JwtAuthenticationStore, can be null
     */
    public abstract  JwtAuthenticationStore getJwtAuthenticationStore(Map<String,Object> claims);

    /**
     * Resolves {@link JwtAuthenticationStore} for given {@link Authentication} instance.
     *
     * @param authentication {@link Authentication} instance
     *
     * @return JwtAuthenticationStore, can be null
     */
    public abstract  JwtAuthenticationStore getJwtAuthenticationStore(Authentication authentication);


    public static ExtensionList<JwtAuthenticationStoreFactory> all(){
        return ExtensionList.lookup(JwtAuthenticationStoreFactory.class);
    }

}

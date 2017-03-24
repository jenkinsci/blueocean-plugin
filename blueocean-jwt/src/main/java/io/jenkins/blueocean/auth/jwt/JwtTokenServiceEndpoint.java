package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This extension point serves the JWT token service endpoint
 *
 * @author Vivek Pandey
 * @see JwtAuthenticationService
 * @see JwkService
 */
public abstract class JwtTokenServiceEndpoint implements ExtensionPoint{
    /**
     * @return Gives JWT endpoint address, e.g. https://example.com/
     */
    public abstract @Nonnull String getHostUrl();

    public static ExtensionList<JwtTokenServiceEndpoint> all(){
        return  ExtensionList.lookup(JwtTokenServiceEndpoint.class);
    }

    public @CheckForNull static JwtTokenServiceEndpoint first(){
        for(JwtTokenServiceEndpoint s:all()){
            return s;
        }
        return null;
    }
}

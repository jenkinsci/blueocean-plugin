package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

/**
 * Participates in the creation of JwtToken
 *
 * @author Vivek Pandey
 */
public abstract class JwtTokenDecorator implements ExtensionPoint {
    /**
     * Called right before {@link JwtToken} is signed.
     *
     * This is an opportunity to add additional claim/header into the token.
     *
     * @param token token to be decorated
     */
    public abstract void decorate(JwtToken token);

    /**
     * Returns all the registered {@link JwtTokenDecorator}s
     */
    public static ExtensionList<JwtTokenDecorator> all() {
        return ExtensionList.lookup(JwtTokenDecorator.class);
    }
}

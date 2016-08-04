package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

/**
 * Participates in the creation of JwtToken
 *
 * @author Vivek Pandey
 */
public abstract class JwtTokenDecorator implements ExtensionPoint {


    /** Decorates {@link JwtToken}
     *
     * @param token token to be decorated
     *
     * @return returns decorated token
     */
    public abstract JwtToken decorate(JwtToken token);

    /**
     * Returns all the registered {@link JwtTokenDecorator}s
     */
    public static ExtensionList<JwtTokenDecorator> all() {
        return ExtensionList.lookup(JwtTokenDecorator.class);
    }
}

package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.blueocean.auth.jwt.impl.JwtTokenVerifierImpl.JwtAuthentication;
import org.acegisecurity.Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * If an incoming HTTP request contains JWT token, pick that up, verifies the integrity, then
 * convert that into {@link JwtAuthentication} so that the rest of Jenkins can process this request
 * with proper identity of the caller.
 *
 * @author Vivek Pandey
 */
public abstract class JwtTokenVerifier implements ExtensionPoint {
    /**
     *
     * @param request
     *      Incoming HTTP request that may (or may not) contains JWT token that we are trying to process
     * @return
     *      null if the request doesn't contain JWT token, in which case the HTTP request will proceed normally
     *      (for example the HTTP session might establish the identity of the user.)
     */
    public abstract Authentication verify(HttpServletRequest request);

    public static ExtensionList<JwtTokenVerifier> all(){
        return ExtensionList.lookup(JwtTokenVerifier.class);
    }
}

package io.jenkins.blueocean.auth.jwt;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import org.acegisecurity.Authentication;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Verifier for JWT token
 *
 * @author Vivek Pandey
 */
public abstract class JwtTokenVerifier implements ExtensionPoint {

    /**
     * On successful verification returns Authentication object representing authentication attached to this JWT token
     *
     * @param request {@link StaplerRequest} to verify
     * @return null if verification fails, an {@link Authentication} instance from JWT token
     */
    public abstract Authentication verify(StaplerRequest request);

    public static ExtensionList<JwtTokenVerifier> all(){
        return ExtensionList.lookup(JwtTokenVerifier.class);
    }
}

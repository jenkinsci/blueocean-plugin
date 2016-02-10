package io.jenkins.blueocean.security;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by ivan on 5/02/16.
 */
final public class IdentityUtils{
    final private static String BLUEOCEAN_IDENTITY_ATTRIBUTE = "blueocean_identity";

    public static Identity getIdentity(ServletRequest request) {
        return (Identity) request.getAttribute(BLUEOCEAN_IDENTITY_ATTRIBUTE);
    }

    public static Identity getIdentity(){
        return getIdentity(Stapler.getCurrentRequest());
    }

    static void setIdentity(ServletRequest request, Identity identity) {
        request.setAttribute(BLUEOCEAN_IDENTITY_ATTRIBUTE, identity);
    }

    static void setIdentity(Identity identity) {
        setIdentity(Stapler.getCurrentRequest(), identity);
    }
}

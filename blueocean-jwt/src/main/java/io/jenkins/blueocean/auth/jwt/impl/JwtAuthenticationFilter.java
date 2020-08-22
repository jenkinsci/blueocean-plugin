package io.jenkins.blueocean.auth.jwt.impl;

import hudson.Extension;
import hudson.init.Initializer;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.PluginServletFilter;
import io.jenkins.blueocean.auth.jwt.JwtTokenVerifier;
import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static io.jenkins.blueocean.commons.BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION_PROPERTY;

/**
 * {@link Filter} that processes JWT token
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class JwtAuthenticationFilter implements Filter {
    /**
     * Used to mark requests that had a valid JWT token.
     */
    private static final String JWT_TOKEN_VALIDATED = JwtAuthenticationFilter.class.getName()+".validated";
    private boolean isJwtEnabled;

    @Initializer(fatal=false)
    public static void init() throws ServletException {
        PluginServletFilter.addFilter(new JwtAuthenticationFilter());
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        /**
         * Initialize jwt enabled flag by reading BLUEOCEAN_FEATURE_JWT_AUTHENTICATION_PROPERTY jvm property
         *
         * {@link io.jenkins.blueocean.commons.BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION} doesn't
         * work in certain test scenario - specially when test sets this JVM property to enable JWT but this class has
         * already been loaded setting it to false.
         *
         */
        this.isJwtEnabled = Boolean.getBoolean(BLUEOCEAN_FEATURE_JWT_AUTHENTICATION_PROPERTY);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        if(!shouldApply(request)) {
            chain.doFilter(req,rsp);
            return;
        }


        Authentication token = verifyToken(request);

        if(token==null) {
            // no JWT token found, which is fine --- we just assume the request is authenticated in other means
            // Some routes that require valid JWT token will check for the presence of JWT token during Stapler
            // request routing, not here.
            chain.doFilter(req,rsp);
            return;
        }

        // run the rest of the request with the new identity
        // create a new context and set it to holder to not clobber existing context
        try (ACLContext ctx = ACL.as(token)) {
            request.setAttribute(JWT_TOKEN_VALIDATED, true);
            chain.doFilter(req,rsp);
        }
    }

    private Authentication verifyToken(HttpServletRequest request) {
        for (JwtTokenVerifier verifier : JwtTokenVerifier.all()) {
            Authentication token = verifier.verify(request);
            if (token != null)
                return token;
        }
        return null;
    }

    /**
     * Returns true for requests that JWT token processing should apply.
     */
    protected boolean shouldApply(HttpServletRequest req) {
        if (!isJwtEnabled)
            return false;

        String path = req.getRequestURI().substring(req.getContextPath().length());
        if(!StringUtils.isBlank(path)){
            path = path.replaceAll("//+", "/"); //skip extra slashes
        }
        return path.startsWith("/blue/")
            || path.startsWith("/sse-gateway/");
    }

    @Override
    public void destroy() {
        // noop
    }

    /**
     * Returns true if the current request had a valid JWT token.
     */
    public static boolean didRequestHaveValidatedJwtToken() {
        return Boolean.TRUE.equals(Stapler.getCurrentRequest().getAttribute(JWT_TOKEN_VALIDATED));
    }
}

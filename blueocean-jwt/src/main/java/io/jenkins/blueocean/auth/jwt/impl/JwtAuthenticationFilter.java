package io.jenkins.blueocean.auth.jwt.impl;

import hudson.Extension;
import hudson.util.PluginServletFilter;
import io.jenkins.blueocean.auth.jwt.JwtTokenVerifier;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.kohsuke.stapler.Stapler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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

    public JwtAuthenticationFilter() throws ServletException {
        PluginServletFilter.addFilter(this);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // noop
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
        SecurityContext sc = new SecurityContextImpl();
        sc.setAuthentication(token);
        SecurityContextHolder.setContext(sc);
        request.setAttribute(JWT_TOKEN_VALIDATED,true);
        try {
            chain.doFilter(req,rsp);
        } finally {
            SecurityContextHolder.clearContext();
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
        if (!BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION)
            return false;

        String path = req.getRequestURI().substring(req.getContextPath().length());
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

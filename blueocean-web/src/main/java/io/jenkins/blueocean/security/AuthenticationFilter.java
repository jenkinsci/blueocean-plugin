package io.jenkins.blueocean.security;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import hudson.util.PluginServletFilter;
import io.jenkins.blueocean.config.ApplicationConfig;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
final public class AuthenticationFilter extends PluginServletFilter{

    private final Cookies cookies;
    private final ApplicationConfig appConfig;

    @Inject
    public AuthenticationFilter(Cookies cookies, ApplicationConfig appConfig) {
        this.cookies = cookies;
        this.appConfig = appConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest)request;
        HttpServletResponse httpResp = (HttpServletResponse)response;
        AuthCookieToken token;
        try {
            token = cookies.readAuthCookieToken(httpReq);
        } catch (Throwable e) {
            // There was a problem decoding the cookie. Delete it and redirect the user home.
            cookies.removeAuthCookieToken(httpResp);
            httpResp.sendRedirect(appConfig.getApplicationPath(httpReq));
            return;
        }
        Identity identity = token == null ? Identity.ANONYMOUS : new Identity(token.user);
        if (identity.isAnonymous() && !httpReq.getPathInfo().startsWith(LoginAction.getPath())) {
            httpResp.sendRedirect(appConfig.getApplicationPath(httpReq) + LoginAction.getPath());
        } else {
            chain.doFilter(new BlueOceanHttpServletRequest(identity, httpReq), httpResp);
        }
    }

}

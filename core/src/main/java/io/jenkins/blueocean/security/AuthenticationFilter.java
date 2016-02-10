package io.jenkins.blueocean.security;

import com.sun.scenario.effect.impl.prism.PrFilterContext;

import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.PluginServletFilter;
import io.jenkins.blueocean.api.profile.ProfileService;
import jenkins.model.Jenkins;

/**
 * Created by ivan on 9/02/16.
 */
@Extension
public class AuthenticationFilter extends PluginServletFilter{

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse resp = (HttpServletResponse)response;
            if(req.getPathInfo().startsWith(LoginAction.getPath())) {
                super.doFilter(request, response, chain);
                return;
            }
            Cookies cookies = new Cookies();
            AuthCookieToken token = cookies.readAuthCookieToken(req);
            if (token == null) {
                resp.sendRedirect(req.getContextPath() + LoginAction.getPath());
            } else {
                IdentityUtils.setIdentity(request, new Identity(token.user));
                super.doFilter(request, response, chain);

            }
        } else {
            super.doFilter(request, response, chain);
        }
    }
}

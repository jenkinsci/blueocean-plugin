package io.jenkins.blueocean.commons.redirect;

import hudson.Extension;
import hudson.init.Initializer;
import hudson.model.User;
import hudson.util.PluginServletFilter;
import jenkins.model.GlobalConfiguration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Extension
public class RedirectFilter implements Filter {
    @Initializer(fatal = false)
    public static void init() throws ServletException {
        PluginServletFilter.addFilter(new RedirectFilter());
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //noop
    }

    @Override
    public void destroy() {
        //noop
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean redirectIndex = false;
        DefaultUserInterfaceGlobalConfiguration userInterfaceGlobalConfiguration = GlobalConfiguration.all().get(DefaultUserInterfaceGlobalConfiguration.class);
        if(userInterfaceGlobalConfiguration != null && InterfaceOption.blueocean.getInterfaceId().equals(userInterfaceGlobalConfiguration.getInterfaceId())) {
            redirectIndex = true;
        }

        User user = User.current();
        if(user != null ) {
            DefaultUserInterfaceUserProperty property = user.getProperty(DefaultUserInterfaceUserProperty.class);
            if(InterfaceOption.blueocean.getInterfaceId().equals(property.getInterfaceId())) {
                redirectIndex = true;
            }
        }
        if(redirectIndex && request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            if("/".equals(httpRequest.getPathInfo())) {
                ((HttpServletResponse) response).sendRedirect(httpRequest.getContextPath() + "/blue");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}

package io.jenkins.blueocean.service.embedded.redirect;

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
        HttpServletRequest request1 = (HttpServletRequest) request;
        if (request instanceof HttpServletRequest && "/".equals(request1.getPathInfo()) && request.getParameter("noDefaultRedirect") == null) {
            String defaultUI = InterfaceOption.classic.getInterfaceId();
            DefaultUserInterfaceGlobalConfiguration userInterfaceGlobalConfiguration = GlobalConfiguration.all().get(DefaultUserInterfaceGlobalConfiguration.class);
            if (userInterfaceGlobalConfiguration != null) {
                defaultUI = userInterfaceGlobalConfiguration.getInterfaceId();
            }

            User user = User.current();
            if (user != null) {
                DefaultUserInterfaceUserProperty property = user.getProperty(DefaultUserInterfaceUserProperty.class);
                if (property != null && !property.getInterfaceId().equals(DefaultUserInterfaceUserProperty.system.getInterfaceId())) {
                    defaultUI = property.getInterfaceId();
                }
            }

            if (InterfaceOption.blueocean.getInterfaceId().equals(defaultUI)) {
                ((HttpServletResponse) response).sendRedirect(request1.getContextPath() + "/blue");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

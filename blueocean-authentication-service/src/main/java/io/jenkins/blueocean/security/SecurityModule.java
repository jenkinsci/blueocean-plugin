package io.jenkins.blueocean.security;

import com.google.inject.AbstractModule;
import hudson.Extension;
import io.jenkins.blueocean.security.config.ApplicationConfig;

/**
 * @author Ivan Meredith
 */
@Extension
public class SecurityModule extends AbstractModule{

    @Override
    protected void configure() {
        bind(ApplicationConfig.class);
        bind(Cookies.class);
        bind(AuthenticationFilter.class);
        bind(AuthenticationAction.class);
        bind(LoginAction.class);
    }
}

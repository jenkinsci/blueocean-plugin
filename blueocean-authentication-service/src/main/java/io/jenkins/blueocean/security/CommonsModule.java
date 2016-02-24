package io.jenkins.blueocean.security;

import com.google.inject.AbstractModule;
import hudson.Extension;
import io.jenkins.blueocean.security.config.ApplicationConfig;

/**
 * Creates guice bindings for classes in blueocean-commons
 */
@Extension
public class CommonsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApplicationConfig.class);
    }
}

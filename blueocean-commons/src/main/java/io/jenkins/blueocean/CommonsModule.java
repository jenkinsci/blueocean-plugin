package io.jenkins.blueocean;

import com.google.inject.AbstractModule;
import hudson.Extension;
import io.jenkins.blueocean.config.ApplicationConfig;

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

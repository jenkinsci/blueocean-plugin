package io.jenkins.blueocean.commons.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import hudson.Extension;
import io.jenkins.blueocean.commons.JsonConverter;

/**
 * Commons Guice module
 *
 * @author Vivek Pandey
 */
@Extension
public class CommonsModule extends AbstractModule {
    @Override
    protected void configure() {
        bindListener(Matchers.any(), new Slf4jTypeListener());
        bind(JsonConverter.class);

    }
}

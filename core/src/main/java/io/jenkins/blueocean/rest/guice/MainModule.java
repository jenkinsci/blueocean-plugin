package io.jenkins.blueocean.rest.guice;

import com.google.inject.AbstractModule;
import io.jenkins.blueocean.rest.JsonHttpResponse;

/**
 * @author Vivek Pandey
 */
public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(JsonConverter.class);
        bind(JsonHttpResponse.class);
    }
}

package io.jenkins.blueocean.service.embedded;

import com.google.inject.AbstractModule;
import hudson.Extension;
import io.jenkins.blueocean.api.pipeline.PipelineService;
import io.jenkins.blueocean.api.profile.ProfileService;

/**
 * @author Ivan Meredith
 */
@Extension
public class EmbeddedModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PipelineService.class).to(EmbeddedPipelineService.class).asEagerSingleton();
    }
}

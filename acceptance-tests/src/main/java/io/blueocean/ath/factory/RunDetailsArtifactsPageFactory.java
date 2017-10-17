package io.blueocean.ath.factory;

import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.pages.blue.RunDetailsArtifactsPage;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;

public interface RunDetailsArtifactsPageFactory {
    RunDetailsArtifactsPage withPipeline(AbstractPipeline pipeline);
}

package io.blueocean.ath.factory;

import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;

public interface RunDetailsPipelinePageFactory {
    RunDetailsPipelinePage withPipeline(AbstractPipeline pipeline);
}

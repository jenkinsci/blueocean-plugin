package io.blueocean.ath.factory;

import io.blueocean.ath.model.Pipeline;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;

public interface RunDetailsPipelinePageFactory {
    RunDetailsPipelinePage withPipeline(Pipeline pipeline);
}

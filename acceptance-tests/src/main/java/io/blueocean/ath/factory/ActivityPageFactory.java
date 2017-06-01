package io.blueocean.ath.factory;

import io.blueocean.ath.model.Pipeline;
import io.blueocean.ath.pages.blue.ActivityPage;

public interface ActivityPageFactory {
    ActivityPage withPipeline(Pipeline pipeline);
}

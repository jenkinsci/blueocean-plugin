package io.blueocean.ath.factory;

import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;

public interface ActivityPageFactory {
    ActivityPage withPipeline(AbstractPipeline pipeline);
}

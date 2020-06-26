package io.blueocean.ath.factory;

import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.pages.blue.RunDetailsFakePage;

public interface RunDetailsFakePageFactory {
    RunDetailsFakePage withPipeline(AbstractPipeline pipeline);
}

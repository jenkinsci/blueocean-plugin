package io.blueocean.ath.factory;

import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.pages.blue.RunDetailsTestsPage;

public interface RunDetailsTestsPageFactory {
    RunDetailsTestsPage withPipeline(AbstractPipeline pipeline);
}

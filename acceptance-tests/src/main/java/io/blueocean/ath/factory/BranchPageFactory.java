package io.blueocean.ath.factory;

import io.blueocean.ath.model.Pipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;

public interface BranchPageFactory {
    BranchPage withPipeline(Pipeline pipeline);
}

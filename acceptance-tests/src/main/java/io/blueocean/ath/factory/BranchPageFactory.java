package io.blueocean.ath.factory;

import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.pages.blue.BranchPage;

public interface BranchPageFactory {
    BranchPage withPipeline(AbstractPipeline pipeline);
}

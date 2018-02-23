package io.blueocean.ath.factory;

import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.pages.blue.PullRequestsPage;

public interface PullRequestsPageFactory {
    PullRequestsPage withPipeline(AbstractPipeline pipeline);
}

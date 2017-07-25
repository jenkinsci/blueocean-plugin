package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.Extension;
import hudson.model.Item;

import javax.annotation.Nonnull;

/**
 * @author cliffmeyers
 */
@Extension(ordinal = -100)
public class GithubEnterpriseScmContentProvider extends GithubScmContentProvider {

    @Nonnull
    @Override
    public String getScmId() {
        return GithubEnterpriseScm.ID;
    }

    @Override
    public boolean support(@Nonnull Item item) {
        boolean isGithubCloud = super.support(item);
        if (!isGithubCloud) {
            return isItemUsingGithubScm(item);
        }
        return false;
    }
}

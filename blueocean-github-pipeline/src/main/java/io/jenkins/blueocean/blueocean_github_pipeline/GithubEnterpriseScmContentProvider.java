package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.Extension;
import hudson.model.Item;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author cliffmeyers
 */
@Extension(ordinal = -100)
public class GithubEnterpriseScmContentProvider extends GithubScmContentProvider {

    @NonNull
    @Override
    public String getScmId() {
        return GithubEnterpriseScm.ID;
    }

    @Override
    public boolean support(@NonNull Item item) {
        boolean isGithubCloud = super.support(item);
        if (!isGithubCloud) {
            return isItemUsingGithubScm(item);
        }
        return false;
    }
}

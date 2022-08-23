package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.AbstractBitbucketScmContentProvider;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author cliffmeyers
 */
@Extension(ordinal = -100)
public class BitbucketCloudScmContentProvider extends AbstractBitbucketScmContentProvider {
    @NonNull
    @Override
    public String getScmId() {
        return BitbucketCloudScm.ID;
    }

    @CheckForNull
    @Override
    public String getApiUrl(@NonNull Item item) {
        BitbucketSCMSource source = getSourceFromItem(item);
        return source != null ? source.getServerUrl() : null;
    }

    @Override
    public boolean support(@NonNull Item item) {
        BitbucketSCMSource source = getSourceFromItem(item);
        return source != null && source.getServerUrl().startsWith(BitbucketCloudScm.API_URL);
    }
}

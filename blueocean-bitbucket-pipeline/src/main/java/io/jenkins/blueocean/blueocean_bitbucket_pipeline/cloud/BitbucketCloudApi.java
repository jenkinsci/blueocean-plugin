package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.base.Preconditions;
import hudson.Extension;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApiFactory;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BitbucketServerApi;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public class BitbucketCloudApi extends BitbucketApi {

    protected BitbucketCloudApi(String apiUrl, StandardUsernamePasswordCredentials credentials) {
        super(apiUrl, credentials);
    }

    @Nonnull
    @Override
    public BbUser getUser() {
        return null;
    }

    @Nonnull
    @Override
    public BbUser getUser(@Nonnull String userName) {
        return null;
    }

    @Nonnull
    @Override
    public BbPage<BbOrg> getOrgs(int start, int limit) {
        return null;
    }

    @Nonnull
    @Override
    public BbOrg getOrg(@Nonnull String projectName) {
        return null;
    }

    @Nonnull
    @Override
    public BbRepo getRepo(@Nonnull String orgId, String repoSlug) {
        return null;
    }

    @Nonnull
    @Override
    public BbPage<BbRepo> getRepos(@Nonnull String projectKey, int pageNumber, int pageSize) {
        return null;
    }

    @Nonnull
    @Override
    public String getContent(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String commitId) {
        return null;
    }

    @Nonnull
    @Override
    public BbSaveContentResponse saveContent(@Nonnull String projectKey, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String content, @Nonnull String commitMessage, @Nonnull String branch, @Nullable String commitId) {
        return null;
    }

    @Override
    public boolean fileExists(String projectKey, String repoSlug, String path, String branch) {
        return false;
    }

    @Override
    public BbBranch getBranch(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String branch) {
        return null;
    }

    @Nonnull
    @Override
    public BbBranch createBranch(@Nonnull String orgId, @Nonnull String repoSlug, Map<String, String> payload) {
        return null;
    }

    @Override
    public BbBranch getDefaultBranch(@Nonnull String orgId, @Nonnull String repoSlug) {
        return null;
    }

    @Override
    public boolean isEmptyRepo(String orgId, @Nonnull String repoSlug) {
        return false;
    }

    @Extension
    public static class BitbucketCloudApiFactory extends BitbucketApiFactory {
        @Override
        public boolean handles(@Nonnull String apiUrl) {
            //We test using wiremock, where api url is always localhost:PORT, so we want to check for bbApiTestMode parameter.
            //bbApiTestMode == "cloud" then its cloud  api mode otherwise its cloud

            StaplerRequest request = Stapler.getCurrentRequest();
            Preconditions.checkNotNull(request);
            String mode = request.getParameter("bbApiTestMode");
            boolean isCloudMode = org.apache.commons.lang.StringUtils.isNotBlank(mode) && mode.equals("cloud");
            return apiUrl.startsWith("https://api.bitbucket.org/") || isCloudMode;
        }

        @Nonnull
        @Override
        public BitbucketApi newInstance(@Nonnull String apiUrl, @Nonnull StandardUsernamePasswordCredentials credentials) {
            return new BitbucketCloudApi(apiUrl, credentials);
        }

    }

}

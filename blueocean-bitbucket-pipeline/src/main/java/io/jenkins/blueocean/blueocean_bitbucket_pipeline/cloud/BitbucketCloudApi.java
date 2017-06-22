package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbProject;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;

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
    public BbPage<BbProject> getProjects(int start, int limit) {
        return null;
    }

    @Nonnull
    @Override
    public BbProject getProject(@Nonnull String projectName) {
        return null;
    }

    @Nonnull
    @Override
    public BbRepo getRepo(@Nonnull String projectKey, String repoSlug) {
        return null;
    }

    @Nonnull
    @Override
    public BbPage<BbRepo> getRepos(@Nonnull String projectKey, int pageNumber, int pageSize) {
        return null;
    }

    @Nonnull
    @Override
    public String getContent(@Nonnull String projectKey, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String commitId) {
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
    public BbBranch getBranch(@Nonnull String projectKey, @Nonnull String repoSlug, @Nonnull String branch) {
        return null;
    }

    @Nonnull
    @Override
    public BbBranch createBranch(@Nonnull String projectKey, @Nonnull String repoSlug, Map<String, String> payload) {
        return null;
    }

    @Override
    public BbBranch getDefaultBranch(@Nonnull String projectKey, @Nonnull String repoSlug) {
        return null;
    }

    @Override
    public boolean isEmptyRepo(String projectKey, @Nonnull String repoSlug) {
        return false;
    }
}

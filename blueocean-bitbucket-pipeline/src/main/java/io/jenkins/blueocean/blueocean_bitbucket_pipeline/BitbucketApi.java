package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import org.antlr.v4.runtime.misc.NotNull;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BitbucketApi {
    protected final String apiUrl;
    protected final StandardUsernamePasswordCredentials credentials;

    protected BitbucketApi(String apiUrl, StandardUsernamePasswordCredentials credentials) {
        this.apiUrl = apiUrl;
        this.credentials = credentials;
    }

    public @Nonnull abstract BbUser getUser();

    public @Nonnull abstract BbUser getUser(@Nonnull String userName);

    public @Nonnull abstract BbPage<BbOrg> getOrgs(int start, int limit);

    public @Nonnull abstract BbOrg getOrg(@Nonnull String projectName);

    public @Nonnull abstract BbRepo getRepo(@Nonnull String orgId, String repoSlug);

    public @Nonnull abstract BbPage<BbRepo> getRepos(@Nonnull String projectKey, int pageNumber, int pageSize);

    public @Nonnull abstract String getContent(@Nonnull String orgId,
                                               @Nonnull String repoSlug,
                                               @Nonnull String path,
                                               @Nonnull String commitId);

    public @Nonnull abstract BbSaveContentResponse saveContent(@Nonnull String projectKey,
                                                               @Nonnull String repoSlug,
                                                               @Nonnull String path,
                                                               @Nonnull String content,
                                                               @Nonnull String commitMessage,
                                                               @Nonnull String branch,
                                                               @Nullable String commitId);

    public abstract boolean fileExists(String projectKey, String repoSlug, String path, String branch);

    public @CheckForNull abstract BbBranch getBranch(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String branch);

    public @Nonnull abstract BbBranch createBranch(@Nonnull String orgId,
                                                   @Nonnull String repoSlug,
                                                   Map<String, String> payload);

    public @CheckForNull abstract BbBranch getDefaultBranch(@Nonnull String orgId, @Nonnull String repoSlug);

    public abstract boolean isEmptyRepo(@NotNull String orgId, @Nonnull String repoSlug);
}

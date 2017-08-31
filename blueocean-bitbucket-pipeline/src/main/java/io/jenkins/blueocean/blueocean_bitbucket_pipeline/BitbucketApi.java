package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import io.jenkins.blueocean.commons.ServiceException;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpResponseException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Bitbucket APIs needed to perform BlueOcean pipeline creation flow.
 *
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BitbucketApi {
    protected final String apiUrl;
    protected final StandardUsernamePasswordCredentials credentials;
    protected final String userName;
    protected final String basicAuthHeaderValue;

    //XXX: To be used for testing to resolve correct factory
    public static final String X_BB_API_TEST_MODE_HEADER="X_BB_API_TEST_MODE_HEADER";


    protected BitbucketApi(@Nonnull String apiUrl, @Nonnull StandardUsernamePasswordCredentials credentials) {
        this.apiUrl = ensureTrailingSlash(apiUrl);
        this.credentials = credentials;
        try {
            this.basicAuthHeaderValue = String.format("Basic %s",
                    Base64.encodeBase64String(String.format("%s:%s", credentials.getUsername(),
                            Secret.toString(credentials.getPassword())).getBytes("UTF-8")));
            this.userName = credentials.getUsername();
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to create basic auth header: "+e.getMessage(), e);
        }
    }

    /**
     * @return {@link BbUser}
     */
    public @Nonnull BbUser getUser(){
        return getUser(this.userName);
    }

    /**
     * Gives user for given userName.
     *
     * @param userSlug name of user, {@link BbUser#getSlug()}
     *
     * @return {@link BbUser}
     */
    public @Nonnull abstract BbUser getUser(@Nonnull String userSlug);

    /**
     * Gives collection of Bitbucket organizations (Project/Team).
     *
     * @param pageNumber page number
     * @param pageSize number of items in a page
     * @return Collection of {@link BbOrg}s
     */
    public @Nonnull abstract BbPage<BbOrg> getOrgs(int pageNumber, int pageSize);

    /**
     * Gives {@link BbOrg} for given project/team name.
     *
     * @param orgName Bitbucket project/team key {@link BbOrg#getKey()}
     *
     * @return {@link BbOrg} instance
     */
    public @Nonnull abstract BbOrg getOrg(@Nonnull String orgName);

    /**
     * Gives {@link BbRepo}
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug repo slug {@link BbRepo#getSlug()}
     * @return {@link BbRepo} instance
     */
    public @Nonnull abstract BbRepo getRepo(@Nonnull String orgId, String repoSlug);

    /**
     * Gives collection of {@link BbRepo}s.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param pageNumber page number
     * @param pageSize page size
     * @return
     */
    public @Nonnull abstract BbPage<BbRepo> getRepos(@Nonnull String orgId, int pageNumber, int pageSize);

    /**
     * Gives content of files in Bitbucket.
     *
     * If given path is not available then {@link io.jenkins.blueocean.commons.ServiceException.NotFoundException}
     * is thrown.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug Bitbucket repo slig {@link BbRepo#getSlug()}
     * @param path path to file in bitbucket repo, e.g. "Jenkinsfile"
     * @param commitId commitId of branch, path will be served off it.
     * @return content
     */
    public @Nonnull abstract String getContent(@Nonnull String orgId,
                                               @Nonnull String repoSlug,
                                               @Nonnull String path,
                                               @Nonnull String commitId);


    /**
     * Saves file to Bitbucket.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug Repo slug {@link BbRepo#getSlug()}
     * @param path destination path, e.g. "Jenkinsfile"
     * @param content file content to save
     * @param commitMessage commit message
     * @param branch branch name. If null then implementation should save on default branch
     * @param commitId if not provided, then file should be saved on tip of branch.
     * @return {@link BbSaveContentResponse} on successful save.
     * @throws io.jenkins.blueocean.commons.ServiceException.ConflictException in case of conflict during save
     */
    public @Nonnull abstract BbSaveContentResponse saveContent(@Nonnull String orgId,
                                                               @Nonnull String repoSlug,
                                                               @Nonnull String path,
                                                               @Nonnull String content,
                                                               @Nonnull String commitMessage,
                                                               @Nullable String branch,
                                                               @Nullable String sourceBranch,
                                                               @Nullable String commitId);

    /**
     * Checks if a file exists in Bitbucket repo.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug repo slug {@link BbRepo#getSlug()}
     * @param path path of file, e.g. "Jenkinsfile"
     * @param branch Bitbucket branch {@link BbBranch#getDisplayId()}
     * @return true if file exists
     */
    public abstract boolean fileExists(@Nonnull String orgId,
                                       @Nonnull String repoSlug,
                                       @Nonnull String path,
                                       @Nonnull String branch);

    /**
     * Gives Bitbucket branch
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug Repo slug {@link BbRepo#getSlug()}
     * @param branch branch name {@link BbBranch#getDisplayId()}
     * @return {@link BbBranch} instance. Could be null if there is no such branch.
     */
    public @CheckForNull abstract BbBranch getBranch(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String branch);

    /**
     * Create branch.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug repo slug {@link BbRepo#getSlug()}
     * @param payload branch payload
     * @return Created branch
     */
    public @Nonnull abstract BbBranch createBranch(@Nonnull String orgId,
                                                   @Nonnull String repoSlug,
                                                   Map<String, String> payload);

    /**
     * Get default branch of a repo.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug Repo slug {@link BbRepo#getSlug()}
     * @return Default branch. null if it's empty repo or if the scm doesn't support default branch concept.
     */
    public @CheckForNull abstract BbBranch getDefaultBranch(@Nonnull String orgId, @Nonnull String repoSlug);

    /**
     * Checks if its empty/un-initialized
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug Repo slug {@link BbRepo#getSlug()}
     * @return true if this is empty or un-initialized repo
     */
    public abstract boolean isEmptyRepo(@NotNull String orgId, @Nonnull String repoSlug);

    /**
     * Converts thrown exception during BB HTTP call in to JSON serializable {@link ServiceException}
     *
     * @param e exception
     * @return {@link ServiceException} instance
     */
    protected ServiceException handleException(Exception e){
        if(e instanceof HttpResponseException){
            return new ServiceException(((HttpResponseException) e).getStatusCode(), e.getMessage(), e);
        }
        return new ServiceException.UnexpectedErrorException(e.getMessage(), e);
    }

    protected static String ensureTrailingSlash(String url){
        if(url.charAt(url.length() - 1) != '/'){
            return url+"/";
        }
        return url;
    }
}

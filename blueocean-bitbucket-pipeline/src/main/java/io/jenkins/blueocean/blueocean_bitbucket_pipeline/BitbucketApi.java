package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import io.jenkins.blueocean.commons.ServiceException;
import org.apache.http.client.HttpResponseException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Map;

/**
 * Bitbucket APIs needed to perform BlueOcean pipeline creation flow.
 *
 * @author Vivek Pandey
 */
@Restricted(NoExternalUse.class)
public abstract class BitbucketApi {

    protected final String apiUrl;
    protected final String userName;
    protected final HttpRequest request;

    protected BitbucketApi(@NonNull String apiUrl, @NonNull StandardUsernamePasswordCredentials credentials) {
        this.apiUrl = ensureTrailingSlash(apiUrl);
        this.request = new HttpRequest.HttpRequestBuilder(apiUrl).credentials(credentials).build();
        this.userName = credentials.getUsername();
    }

    /**
     * @return {@link BbUser}
     */
    public @NonNull BbUser getUser(){
        return getUser(this.userName);
    }

    /**
     * Gives user for given userName.
     *
     * @param userSlug name of user, {@link BbUser#getSlug()}
     *
     * @return {@link BbUser}
     */
    public @NonNull abstract BbUser getUser(@NonNull String userSlug);

    /**
     * Gives collection of Bitbucket organizations (Project/Team).
     *
     * @param pageNumber page number
     * @param pageSize number of items in a page
     * @return Collection of {@link BbOrg}s
     */
    public @NonNull abstract BbPage<BbOrg> getOrgs(int pageNumber, int pageSize);

    /**
     * Gives {@link BbOrg} for given project/team name.
     *
     * @param orgName Bitbucket project/team key {@link BbOrg#getKey()}
     *
     * @return {@link BbOrg} instance
     */
    public @NonNull abstract BbOrg getOrg(@NonNull String orgName);

    /**
     * Gives {@link BbRepo}
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug repo slug {@link BbRepo#getSlug()}
     * @return {@link BbRepo} instance
     */
    public @NonNull abstract BbRepo getRepo(@NonNull String orgId, String repoSlug);

    /**
     * Gives collection of {@link BbRepo}s.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param pageNumber page number
     * @param pageSize page size
     * @return
     */
    public @NonNull abstract BbPage<BbRepo> getRepos(@NonNull String orgId, int pageNumber, int pageSize);

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
    public @NonNull abstract String getContent(@NonNull String orgId,
                                               @NonNull String repoSlug,
                                               @NonNull String path,
                                               @NonNull String commitId);


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
    public @NonNull abstract BbSaveContentResponse saveContent(@NonNull String orgId,
                                                               @NonNull String repoSlug,
                                                               @NonNull String path,
                                                               @NonNull String content,
                                                               @NonNull String commitMessage,
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
    public abstract boolean fileExists(@NonNull String orgId,
                                       @NonNull String repoSlug,
                                       @NonNull String path,
                                       @NonNull String branch);

    /**
     * Gives Bitbucket branch
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug Repo slug {@link BbRepo#getSlug()}
     * @param branch branch name {@link BbBranch#getDisplayId()}
     * @return {@link BbBranch} instance. Could be null if there is no such branch.
     */
    public @CheckForNull abstract BbBranch getBranch(@NonNull String orgId, @NonNull String repoSlug, @NonNull String branch);

    /**
     * Create branch.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug repo slug {@link BbRepo#getSlug()}
     * @param payload branch payload
     * @return Created branch
     */
    public @NonNull abstract BbBranch createBranch(@NonNull String orgId,
                                                   @NonNull String repoSlug,
                                                   Map<String, String> payload);

    /**
     * Get default branch of a repo.
     *
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug Repo slug {@link BbRepo#getSlug()}
     * @return Default branch. null if it's empty repo or if the scm doesn't support default branch concept.
     */
    public @CheckForNull abstract BbBranch getDefaultBranch(@NonNull String orgId, @NonNull String repoSlug);

    /**
     * Checks if its empty/un-initialized
     * @param orgId Bitbucket project/team key {@link BbOrg#getKey()}
     * @param repoSlug Repo slug {@link BbRepo#getSlug()}
     * @return true if this is empty or un-initialized repo
     */
    public abstract boolean isEmptyRepo(@NonNull String orgId, @NonNull String repoSlug);

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

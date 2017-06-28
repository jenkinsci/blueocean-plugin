package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
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


    protected BitbucketApi(String apiUrl, StandardUsernamePasswordCredentials credentials) {
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

    public @Nonnull BbUser getUser(){
        return getUser(this.userName);
    }

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
                                                               @Nullable String branch,
                                                               @Nullable String commitId);

    public abstract boolean fileExists(String projectKey, String repoSlug, String path, String branch);

    public @CheckForNull abstract BbBranch getBranch(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String branch);

    public @Nonnull abstract BbBranch createBranch(@Nonnull String orgId,
                                                   @Nonnull String repoSlug,
                                                   Map<String, String> payload);

    public @CheckForNull abstract BbBranch getDefaultBranch(@Nonnull String orgId, @Nonnull String repoSlug);

    public abstract boolean isEmptyRepo(@NotNull String orgId, @Nonnull String repoSlug);

    protected ServiceException handleException(Exception e){
        if(e instanceof HttpResponseException){
            return new ServiceException(((HttpResponseException) e).getStatusCode(), e.getMessage(), e);
        }
        return new ServiceException.UnexpectedErrorException(e.getMessage(), e);
    }


    private String ensureTrailingSlash(String url){
        if(url.charAt(url.length() - 1) != '/'){
            return url+"/";
        }
        return url;
    }
}

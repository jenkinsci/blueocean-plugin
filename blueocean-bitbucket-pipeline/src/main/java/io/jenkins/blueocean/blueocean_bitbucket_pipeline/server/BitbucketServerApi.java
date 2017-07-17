package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;


import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import hudson.Extension;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApiFactory;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.Messages;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerProject;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerUser;
import io.jenkins.blueocean.commons.ServiceException;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.jenkins.blueocean.commons.JsonConverter.om;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerApi extends BitbucketApi {
    public static final DefaultArtifactVersion MINIMUM_SUPPORTED_VERSION=new DefaultArtifactVersion("5.2.0");
    private final String baseUrl;

    //package private for testing
    BitbucketServerApi(@Nonnull String hostUrl, @Nonnull StandardUsernamePasswordCredentials credentials) {
        super(hostUrl, credentials);
        this.baseUrl=apiUrl+"rest/api/1.0/";
    }

    /**
     * Gives Bitbucket server version
     * @param apiUrl API url of Bitbucket server
     * @return version of Bitbucket server
     */
    public @Nonnull static String getVersion(@Nonnull String apiUrl){
        try {
            apiUrl = ensureTrailingSlash(apiUrl);
            InputStream inputStream = Request.Get(apiUrl+"rest/api/1.0/application-properties")
                    .execute().returnContent().asStream();
            Map<String,String> resp =  om.readValue(inputStream, new TypeReference<Map < String, String >>() {
            });
            String version = resp.get("version");
            if(StringUtils.isBlank(version)){
                throw new ServiceException.PreconditionRequired("Unsupported Bitbucket server, no version information could be determined");
            }
            return version;
        } catch (IOException e) {
            if(e instanceof HttpResponseException){
                throw new ServiceException(((HttpResponseException) e).getStatusCode(), e.getMessage(), e);
            }
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

    @Override
    public @Nonnull BbUser getUser(@Nonnull String userName){
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s",baseUrl+"users", userName))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, BbServerUser.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }


    @Override
    public @Nonnull
    BbPage<BbOrg> getOrgs(int pageNumber, int pageSize){
        try {
            InputStream inputStream = Request.Get(String.format("%s?start=%s&limit=%s",baseUrl+"projects/", toStart(pageNumber, pageSize), pageSize))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, new TypeReference<BbServerPage<BbServerProject>>(){});
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Override
    public @Nonnull
    BbOrg getOrg(@Nonnull String projectName){
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s",baseUrl+"projects", projectName))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, BbServerProject.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }


    @Override
    public @Nonnull
    BbPage<BbRepo> getRepos(@Nonnull String projectKey, int pageNumber, int pageSize){
        try {
            InputStream inputStream = Request.Get(String.format("%s?start=%s&limit=%s",baseUrl+"projects/"+projectKey+"/repos/", toStart(pageNumber, pageSize), pageSize))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, new TypeReference<BbServerPage<BbServerRepo>>(){});
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbRepo getRepo(@Nonnull String orgId, @Nonnull String repoSlug) {
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s/repos/%s/",baseUrl+"projects", orgId, repoSlug))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, BbServerRepo.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Override
    public @Nonnull String getContent(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String commitId){
        List<String> content = new ArrayList<>();
        getAndBuildContent(orgId, repoSlug, path, commitId,0, 500, content); //default size as in bitbucket API
        return Joiner.on('\n').join(content);
    }

    @Override
    public @Nonnull
    BbSaveContentResponse saveContent(@Nonnull String projectKey,
                                      @Nonnull String repoSlug,
                                      @Nonnull String path,
                                      @Nonnull String content,
                                      @Nonnull String commitMessage,
                                      @Nullable String branch,
                                      @Nullable String commitId){
        try {
            String version = getVersion(apiUrl);
            if(!isSupportedVersion(version)){
                throw new ServiceException.PreconditionRequired(
                        Messages.bbserver_version_validation_error(
                                apiUrl, version, BitbucketServerApi.MINIMUM_SUPPORTED_VERSION));
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addTextBody("content", content)
                    .addTextBody("message", commitMessage);

            if(!StringUtils.isBlank(branch)){
                builder.addTextBody("branch", branch);
            }
            if(org.apache.commons.lang.StringUtils.isNotBlank(commitId)){
                   builder.addTextBody("sourceCommitId", commitId);
            }
            HttpEntity entity = builder.build();
            InputStream inputStream = Request.Put(String.format("%s/%s/repos/%s/browse/%s",baseUrl+"projects",projectKey,repoSlug, path))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .body(entity)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, BbServerSaveContentResponse.class);
        } catch (IOException e) {
            //there might be 409 error if same content is submitted for save with a given commitId
            // we ignore error and return the response as if it was saved successfully
            if(commitId != null && e instanceof HttpResponse && ((HttpResponse) e).getStatusLine().getStatusCode() == 409){
                return new BbServerSaveContentResponse(commitId);
            }
            throw handleException(e);
        }
    }

    @Override
    public boolean fileExists(@Nonnull String projectKey, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String branch){
        try {
            URIBuilder uriBuilder = new URIBuilder(String.format("%s/%s/repos/%s/browse/%s",baseUrl+"projects",
                    projectKey, repoSlug, path));

            if(branch != null){
                uriBuilder.addParameter("at", "refs/heads/"+branch);
            }
            Response response = Request.Head(uriBuilder.build())
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute();
            return response.returnResponse().getStatusLine().getStatusCode() == 200;
        } catch (IOException | URISyntaxException e) {
            throw handleException(e);
        }
    }

    @Override
    public @CheckForNull
    BbBranch getBranch(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String branch){
        try {
            URIBuilder uriBuilder = new URIBuilder(String.format("%s/%s/repos/%s/branches/",baseUrl+"projects",
                    orgId, repoSlug));

            uriBuilder.addParameter("filterText", branch);
            BbServerPage<BbServerBranch> branches = om.readValue(Request.Get(uriBuilder.build())
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream(), new TypeReference<BbServerPage<BbServerBranch>>() {
            });
            String expectedId = "refs/heads/"+branch;
            for(BbServerBranch b : branches.getValues()){
                if(b.getId().equals(expectedId)){
                    return b;
                }
            }
            return null;
        } catch (IOException | URISyntaxException e) {
            throw handleException(e);
        }
    }

    @Override
    @Nonnull
    public BbBranch createBranch(@Nonnull String orgId, @Nonnull String repoSlug, Map<String, String> payload){
        try {
            return om.readValue(Request.Post(String.format("%s/%s/repos/%s/branches/", baseUrl + "projects",
                    orgId, repoSlug))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .bodyString(om.writeValueAsString(payload), ContentType.APPLICATION_JSON)
                    .execute().returnContent().asStream(), BbServerBranch.class);
        }catch (IOException e){
            throw handleException(e);
        }
    }

    @Override
    public @CheckForNull
    BbBranch getDefaultBranch(@Nonnull String orgId, @Nonnull String repoSlug){
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s/repos/%s/branches/default",baseUrl+"projects",
                    orgId, repoSlug))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, new TypeReference<BbServerBranch>() {
            });
        }catch (HttpResponseException e) {
            if(e.getStatusCode() == 404){ //empty repo gives 404, we ignore these
                return null;
            }
            throw handleException(e);
        }catch (IOException e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isEmptyRepo(@NotNull String orgId, @Nonnull String repoSlug){
        try {
            URIBuilder uriBuilder = new URIBuilder(String.format("%s/%s/repos/%s/branches/default",baseUrl+"projects",
                    orgId, repoSlug));

            Response response = Request.Head(uriBuilder.build())
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute();
            return response.returnResponse().getStatusLine().getStatusCode() == 404;
        } catch (IOException | URISyntaxException e) {
            throw handleException(e);
        }
    }

    private void getAndBuildContent(@Nonnull String projectKey, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String commitId, int start, int limit, @Nonnull final List<String> lines){
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s/repos/%s/browse/%s?at=%s&start=%s&limit=%s",baseUrl+"projects",
                    projectKey, repoSlug, path, commitId, start, limit))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();

            Map<String,Object> content = om.readValue(inputStream, new TypeReference<Map<String,Object>>(){});
            List<Map<String, String>> lineMap = (List<Map<String, String>>) content.get("lines");
            collectLines(lineMap, lines);
            int size = (int) content.get("size");
            if(!(boolean)content.get("isLastPage")){
                getAndBuildContent(projectKey, repoSlug, path, commitId, start+size, limit, lines);
            }
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    private void collectLines(List<Map<String,String>> lineMap, final List<String> lines){
        lines.addAll(Lists.transform(lineMap, new Function<Map<String,String>, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Map<String, String> input) {
                if(input != null){
                    String text = input.get("text");
                    if(text != null){
                        return text;
                    }
                }
                return null;
            }
        }));
    }

    private void assertDefaultBranch(BbBranch defaultBranch, String projectKey, String repo){
        if(defaultBranch == null){
            throw new ServiceException.BadRequestException(
                    String.format("No default branch on project %s, repo %s. Please resubmit request with content.branch",
                            projectKey,repo));
        }
    }

    private int toStart(int pageNumber, int pageSize){
        int start = pageSize*(pageNumber-1);
        if(start < 0){
            start = 0;
        }
        return start;
    }

    /**
     * Tells whether given version is supported version.
     *
     * @param version version of Bitbucket server to test
     * @return true if supported false otherwise
     * @see #getVersion(String)
     */
    public static boolean isSupportedVersion(@Nonnull String version){
        return new DefaultArtifactVersion(version).compareTo(MINIMUM_SUPPORTED_VERSION) >= 0;
    }

    @Extension
    public static class BitbucketServerApiFactory extends BitbucketApiFactory{
        @Override
        public boolean handles(@Nonnull String apiUrl) {
            StaplerRequest request = Stapler.getCurrentRequest();
            Preconditions.checkNotNull(request);
            String mode=request.getHeader(X_BB_API_TEST_MODE_HEADER);
            boolean isCloudMode =  StringUtils.isNotBlank(mode) && mode.equals("cloud"); //used for testing
            return !isCloudMode && !apiUrl.startsWith("https://bitbucket.org") && !apiUrl.startsWith("https://api.bitbucket.org");
        }

        @Nonnull
        @Override
        public BitbucketApi create(@Nonnull String apiUrl, @Nonnull StandardUsernamePasswordCredentials credentials) {
            return new BitbucketServerApi(apiUrl, credentials);
        }

    }
}

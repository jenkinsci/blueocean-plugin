package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;


import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.util.VersionNumber;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApiFactory;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.HttpRequest;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.HttpResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.Messages;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerProject;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerUser;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import static io.jenkins.blueocean.commons.JsonConverter.om;

/**
 * @author Vivek Pandey
 */
public class BitbucketServerApi extends BitbucketApi {
    public static final VersionNumber MINIMUM_SUPPORTED_VERSION = new VersionNumber("5.2.0");
    private final String baseUrl;
    private final StandardUsernamePasswordCredentials credentials;

    //package private for testing
    BitbucketServerApi(@Nonnull String hostUrl, @Nonnull StandardUsernamePasswordCredentials credentials) {
        super(hostUrl, credentials);
        this.baseUrl=apiUrl+"rest/api/1.0/";
        this.credentials = credentials;
    }

    /**
     * Gives Bitbucket server version
     * @param apiUrl API url of Bitbucket server
     * @return version of Bitbucket server
     */
    public @Nonnull static String getVersion(@Nonnull String apiUrl){
        try {
            apiUrl = ensureTrailingSlash(apiUrl);

            HttpRequest request = new HttpRequest.HttpRequestBuilder(apiUrl).build();
            HttpResponse response = request.get(apiUrl+"rest/api/1.0/application-properties");
            int status = response.getStatus();
            if((status >= 301 && status <= 303) || status == 307 || status == 308) {
                String location = response.getHeader("Location");
                String error = String.format("%s is invalid. Bitbucket server sent redirect response", apiUrl);
                if(StringUtils.isNotBlank(location)) {
                    URL url = new URL(location);
                    String host = url.getHost();
                    int port = url.getPort();
                    String protocol = url.getProtocol();
                    String baseUrl = protocol + "://"+host + ((port == -1) ? "/" : port+"/");
                    error += " with location at: "+baseUrl;
                }
                error += ". \nPlease use correct Bitbucket Server endpoint.";
                throw new ServiceException(status, error);
            }
            InputStream inputStream = response.getContent();
            Map<String,String> resp =  om.readValue(inputStream, new TypeReference<Map < String, String >>() {
            });
            String version = resp.get("version");
            if(StringUtils.isBlank(version)){
                throw new ServiceException.PreconditionRequired("Unsupported Bitbucket server, no version information could be determined");
            }
            return version;
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

    @Override
    public @Nonnull BbServerUser getUser(@Nonnull String userName){
        try {
            InputStream inputStream = request.get(String.format("%s/%s",baseUrl+"users", userName)).getContent();
            return om.readValue(inputStream, BbServerUser.class);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

    @Override
    public @Nonnull
    BbPage<BbOrg> getOrgs(int pageNumber, int pageSize){
        try {
            if(pageNumber <= 0){
                pageNumber = 1;
            }

            if(pageSize <=0){
                pageSize = PagedResponse.DEFAULT_LIMIT;
            }
            InputStream inputStream = request.get(String.format("%s?start=%s&limit=%s",baseUrl+"projects/",
                    toStart(pageNumber, pageSize), pageSize))
                    .getContent();
            BbPage<BbOrg> page =  om.reader().forType(new TypeReference<BbServerPage<BbServerProject>>(){}).readValue(inputStream);
            if(pageNumber == 1){ //add user org as the first org on first page
                BbServerUser user = getUser(userName);
                List<BbOrg> teams = new ArrayList<>();
                teams.add(user.toPersonalProject());
                teams.addAll(page.getValues());
                return new BbServerPage<>(page.getStart(), page.getLimit(), page.getSize()+1, teams, page.isLastPage());
            }
            return page;
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Override
    public @Nonnull
    BbOrg getOrg(@Nonnull String projectName){
        try {
            InputStream inputStream = request.get(String.format("%s/%s",baseUrl+"projects", projectName))
                    .getContent();
            return om.readValue(inputStream, BbServerProject.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }


    @Override
    public @Nonnull
    BbPage<BbRepo> getRepos(@Nonnull String projectKey, int pageNumber, int pageSize){
        try {
            InputStream inputStream = request.get(String.format("%s?start=%s&limit=%s",baseUrl+"projects/"+projectKey+"/repos/", toStart(pageNumber, pageSize), pageSize))
                    .getContent();
            return om.reader().forType(new TypeReference<BbServerPage<BbServerRepo>>(){}).readValue(inputStream);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbRepo getRepo(@Nonnull String orgId, @Nonnull String repoSlug) {
        try {
            InputStream inputStream = request.get(String.format("%s/%s/repos/%s/",baseUrl+"projects", orgId, repoSlug))
                    .getContent();
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
                                      @Nullable String sourceBranch,
                                      @Nullable String commitId){
        try {
            String version = getVersion(apiUrl);
            if(!isSupportedVersion(version)){
                throw new ServiceException.PreconditionRequired(
                        Messages.bbserver_version_validation_error(
                                version, BitbucketServerApi.MINIMUM_SUPPORTED_VERSION));
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addTextBody("content", content)
                    .addTextBody("message", commitMessage);

            if(StringUtils.isNotBlank(branch)){
                builder.addTextBody("branch", branch);
            }
            if(StringUtils.isNotBlank(sourceBranch)){
                builder.addTextBody("sourceBranch", sourceBranch);
            }
            if(org.apache.commons.lang.StringUtils.isNotBlank(commitId)){
                   builder.addTextBody("sourceCommitId", commitId);
            }
            HttpEntity entity = builder.build();
            HttpResponse response = request.put(String.format("%s/%s/repos/%s/browse/%s",baseUrl+"projects",projectKey,repoSlug, path), entity);

            //there might be 409 error if same content is submitted for save with a given commitId
            // we ignore error and return the response as if it was saved successfully
            if(commitId != null && response.getStatus() == 409){
                return new BbServerSaveContentResponse(commitId);
            }
            InputStream inputStream = response.getContent();
            return om.readValue(inputStream, BbServerSaveContentResponse.class);
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(),e);
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
            HttpResponse response = request.head(uriBuilder.build().toString());
            return response.getStatus() == 200;
        } catch (URISyntaxException e) {
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
            BbServerPage<BbServerBranch> branches = om.readValue(request.get(uriBuilder.build().toString())
                    .getContent(), new TypeReference<BbServerPage<BbServerBranch>>() {
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

            return om.readValue(request.post(String.format("%s/%s/repos/%s/branches/", baseUrl + "projects",
                    orgId, repoSlug), new ByteArrayEntity(om.writeValueAsBytes(payload), ContentType.APPLICATION_JSON))
                    .getContent(), BbServerBranch.class);
        }catch (IOException e){
            throw handleException(e);
        }
    }

    @Override
    public @CheckForNull
    BbBranch getDefaultBranch(@Nonnull String orgId, @Nonnull String repoSlug){
        try {
            HttpResponse response = request.get(String.format("%s/%s/repos/%s/branches/default",baseUrl+"projects",
                    orgId, repoSlug));
            int status = response.getStatus();
            //With 5.6.0 its 204, before that it was 404
            if(status == 404 || status == 204){
                return null; //empty repo gives 404, we ignore these
            }
            InputStream inputStream = response.getContent();
            return om.readValue(inputStream, new TypeReference<BbServerBranch>() {
            });
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isEmptyRepo(@Nonnull String orgId, @Nonnull String repoSlug){
        try {
            URIBuilder uriBuilder = new URIBuilder(String.format("%s/%s/repos/%s/branches/default",baseUrl+"projects",
                    orgId, repoSlug));

            HttpResponse response = request.head(uriBuilder.build().toString());
            int status = response.getStatus();
            return status == 404 || status == 204; //with BB server 5.6.0 204 is returned for empty repo
        } catch (URISyntaxException e) {
            throw handleException(e);
        }
    }

    private void getAndBuildContent(@Nonnull String projectKey, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String commitId, int start, int limit, @Nonnull final List<String> lines){
        try {
            InputStream inputStream = request.get(String.format("%s/%s/repos/%s/browse/%s?at=%s&start=%s&limit=%s",baseUrl+"projects",
                    projectKey, repoSlug, path, commitId, start, limit)).getContent();

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
        return new VersionNumber(version).isNewerThanOrEqualTo(MINIMUM_SUPPORTED_VERSION);
    }

    @Extension
    public static class BitbucketServerApiFactory extends BitbucketApiFactory{
        @Override
        public boolean handles(@Nonnull String scmId) {
            return scmId.equals(BitbucketServerScm.ID);
        }

        @Nonnull
        @Override
        public BitbucketApi create(@Nonnull String apiUrl, @Nonnull StandardUsernamePasswordCredentials credentials) {
            return new BitbucketServerApi(apiUrl, credentials);
        }

    }
}

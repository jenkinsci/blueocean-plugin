package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import hudson.Extension;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApiFactory;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudTeam;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.model.BbCloudUser;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.pageable.PagedResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static io.jenkins.blueocean.commons.JsonConverter.om;

/**
 * @author Vivek Pandey
 */
public class BitbucketCloudApi extends BitbucketApi {
    private final String baseUrl;

    protected BitbucketCloudApi(String apiUrl, StandardUsernamePasswordCredentials credentials) {
        super(apiUrl, credentials);
        this.baseUrl = this.apiUrl+"2.0/";
    }

    @Nonnull
    @Override
    public BbUser getUser(@Nonnull String userName) {
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s",baseUrl+"users", userName))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, BbCloudUser.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbPage<BbOrg> getOrgs(int start, int limit) {
        try {
            /*
             * Bitbucket teams API work with three roles: admin, contributor, member
             *
             * We default to 'contributor' role, because we only want to only list teams that have at least one repo
             * where user has WRITE access.
             *
             * see: https://developer.atlassian.com/bitbucket/api/2/reference/resource/teams
             */
            InputStream inputStream = Request.Get(String.format("%s&page=%s&pagelen=%s",baseUrl+"teams/?role=contributor", getPage(start,limit),limit))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, new TypeReference<BbCloudPage<BbCloudTeam>>(){});
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    private int getPage(int start, int limit){
        if(limit <= 0){
            limit = PagedResponse.DEFAULT_LIMIT;
        }
        if(start <0){
            start = 0;
        }
        return (start/limit) + 1;
    }

    @Nonnull
    @Override
    public BbOrg getOrg(@Nonnull String orgName) {
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s",baseUrl+"teams", orgName))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, BbCloudTeam.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbRepo getRepo(@Nonnull String orgId, String repoSlug) {
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s",baseUrl+"repositories/"+orgId, repoSlug))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, BbCloudRepo.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbPage<BbRepo> getRepos(@Nonnull String orgId, int pageNumber, int pageSize) {
        try {
            InputStream inputStream = Request.Get(String.format("%s?page=%s&limit=%s",baseUrl+"repositories/"+orgId, pageNumber,pageSize))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, new TypeReference<BbCloudPage<BbCloudRepo>>(){});
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public String getContent(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String commitId) {
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s/%s/src/%s/%s",baseUrl+"repositories",orgId,
                    repoSlug, commitId, path))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbSaveContentResponse saveContent(@Nonnull String orgId,
                                             @Nonnull String repoSlug,
                                             @Nonnull String path,
                                             @Nonnull String content,
                                             @Nonnull String commitMessage,
                                             @Nullable String branch,
                                             @Nullable String commitId) {
        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                        .addTextBody(path, content)
                    .addTextBody("message", commitMessage);

            if(StringUtils.isNotBlank(branch)){
                builder.addTextBody("branch", branch);
            }

            if(org.apache.commons.lang.StringUtils.isNotBlank(commitId)){
                builder.addTextBody("parents", commitId);
            }
            HttpEntity entity = builder.build();
            Response response = Request.Post(String.format("%s/%s/%s/src",baseUrl+"repositories",orgId,repoSlug))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .body(entity)
                    .execute();
            HttpResponse resp = response.returnResponse();
            int status = resp.getStatusLine().getStatusCode();
            if(status == 201){
                String location = resp.getFirstHeader("Location").getValue();
                String cid = location.substring(location.lastIndexOf("/")+1);
                return new BbCloudSaveContentResponse(cid);
            }else{
                throw new ServiceException.UnexpectedErrorException("Failed to save file: "+path+" server returned status: "+status);
            }
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean fileExists(String projectKey, String repoSlug, String path, String branch) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public BbBranch getBranch(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String branch) {
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s/refs/branches/%s?fields\\=target.hash,target.repository.mainbranch.name,target.repository.\\*,target.repository.owner.\\*,target.repository.owner.links.avatar.href,name",
                    baseUrl+"repositories/"+orgId,
                    repoSlug,
                    branch))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            return om.readValue(inputStream, BbCloudBranch.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbBranch createBranch(@Nonnull String orgId, @Nonnull String repoSlug, Map<String, String> payload) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public BbBranch getDefaultBranch(@Nonnull String orgId, @Nonnull String repoSlug) {
        try {
            InputStream inputStream = Request.Get(String.format("%s/%s/?fields=mainbranch.*,mainbranch.target.*,mainbranch.target.repository.*,mainbranch.target.repository.mainbranch.name,mainbranch.target.repository.owner.*,mainbranch.target.repository.owner.links.avatar.*",
                    baseUrl+"repositories/"+orgId,
                    repoSlug))
                    .addHeader("Authorization", basicAuthHeaderValue)
                    .execute().returnContent().asStream();
            Map<String, BbCloudBranch> resp = om.readValue(inputStream, new TypeReference<Map<String, BbCloudBranch>>() {});
            return resp.get("mainbranch");
        } catch (IOException e) {
            throw handleException(e);
        }

    }

    @Override
    public boolean isEmptyRepo(String orgId, @Nonnull String repoSlug) {
        throw new NotImplementedException("Not implemented");
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

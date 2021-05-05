package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.core.type.TypeReference;
import hudson.Extension;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApiFactory;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.HttpResponse;
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
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.jenkins.blueocean.commons.JsonConverter.om;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Vivek Pandey
 */
public class BitbucketCloudApi extends BitbucketApi {
    static Logger LOGGER = Logger.getLogger(BitbucketCloudApi.class.getName());
    private final String baseUrl;

    protected BitbucketCloudApi(String apiUrl, StandardUsernamePasswordCredentials credentials) {
        super(apiUrl, credentials);
        this.baseUrl = this.apiUrl+"api/2.0/";
    }

    private String encodePath(String path) {
        try {
            path = URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Error encoding parameter " + e.getMessage(), e);
        }
        return path;
    }

    @Nonnull
    @Override
    public BbUser getUser() {
        try {
            InputStream inputStream = request.get(baseUrl+"user").getContent();
            return om.readValue(inputStream, BbCloudUser.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbUser getUser(@Nonnull String userName) {
        try {
            InputStream inputStream = request.get(String.format("%s/%s",baseUrl+"users", encodePath(userName))).getContent();
            return om.readValue(inputStream, BbCloudUser.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbPage<BbOrg> getOrgs(int pageNumber, int pageSize) {
        try {
            /*
             * Bitbucket teams API work with three roles: admin, contributor, member
             *
             * We default to 'contributor' role, because we only want to only list teams that have at least one repo
             * where user has WRITE access.
             *
             * see: https://developer.atlassian.com/bitbucket/api/2/reference/resource/teams
             */
            if(pageNumber <= 0){
                pageNumber = 1;
            }

            if(pageSize <=0){
                pageSize = PagedResponse.DEFAULT_LIMIT;
            }
            InputStream inputStream = request.get(String.format("%s&page=%s&pagelen=%s",baseUrl+"teams/?role=contributor",
                    pageNumber,pageSize)).getContent();
            BbPage<BbOrg> page =  om.reader().forType(new TypeReference<BbCloudPage<BbCloudTeam>>(){}).readValue(inputStream);
            if(pageNumber == 1){ //add user org as the first org on first page
                BbUser user = getUser();
                if(page instanceof BbCloudPage) {
                    List<BbOrg> teams = new ArrayList<>();
                    teams.add(new BbCloudTeam(user.getSlug(), user.getDisplayName(), user.getAvatar()));
                    int newSize = page.getSize() + 1;
                    int newPageLength = page.getLimit();
                    if (page.getSize() > page.getLimit()) {
                        newPageLength++;
                    }
                    teams.addAll(page.getValues());
                    return new BbCloudPage<>(newPageLength, pageNumber, newSize, ((BbCloudPage) page).getNext(), teams);
                }
            }
            return page;
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbOrg getOrg(@Nonnull String orgName) {
        try {
            BbUser user = getUser();
            // If user org, get user and return BbCloudTeam model
            if(orgName.equalsIgnoreCase(user.getSlug())){
                return new BbCloudTeam(user.getSlug(), user.getDisplayName(), user.getAvatar());
            }
            InputStream inputStream = request.get(String.format("%s/%s",baseUrl+"teams", encodePath(orgName))).getContent();
            return om.readValue(inputStream, BbCloudTeam.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbRepo getRepo(@Nonnull String orgId, String repoSlug) {
        try {
            InputStream inputStream = request.get(String.format("%s/%s",baseUrl+"repositories/"+encodePath(orgId), repoSlug))
                    .getContent();
            return om.readValue(inputStream, BbCloudRepo.class);
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public BbPage<BbRepo> getRepos(@Nonnull String orgId, int pageNumber, int pageSize) {
        try (InputStream inputStream = request.get(String.format("%s?page=%s&limit=%s",
                                                                 baseUrl+"repositories/"+encodePath(orgId),
                                                                 pageNumber, pageSize)).getContent()){
            return om.reader().forType(new TypeReference<BbCloudPage<BbCloudRepo>>(){}).readValue( inputStream );
        } catch (IOException e) {
            throw handleException(e);
        }
    }

    @Nonnull
    @Override
    public String getContent(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String path, @Nonnull String commitId) {
        try {
            InputStream inputStream = request.get(String.format("%s/%s/%s/src/%s/%s",baseUrl+"repositories",encodePath(orgId),
                    repoSlug, commitId, path)).getContent();
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
                                             @Nullable String sourceBranch,
                                             @Nullable String commitId) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .addTextBody(path, content)
                .addTextBody("message", commitMessage);

        if(isNotBlank(branch)){
            builder.addTextBody("branch", branch);
        }

        if(isNotBlank(commitId)){
            builder.addTextBody("parents", commitId);
        }
        HttpEntity entity = builder.build();
        HttpResponse response = request.post(String.format("%s/%s/%s/src",baseUrl+"repositories",encodePath(orgId),encodePath(repoSlug)), entity);
        int status = response.getStatus();
        if(status == 201){
            String location = response.getHeader("Location");
            if(location == null){
                throw new ServiceException.UnexpectedErrorException("Location header is missing on save content response");
            }
            String cid = location.substring(location.lastIndexOf("/") + 1);
            return new BbCloudSaveContentResponse(cid);
        }else{
            throw new ServiceException.UnexpectedErrorException("Failed to save file: "+path+" server returned status: "+status);
        }
    }

    @Override
    public boolean fileExists(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String path,  @Nonnull String branch) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public BbBranch getBranch(@Nonnull String orgId, @Nonnull String repoSlug, @Nonnull String branch) {
        try {
            HttpResponse response = request.get(String.format("%s/%s/refs/branches/%s?fields=target.hash,target.repository.mainbranch.name,target.repository.*,target.repository.owner.*,target.repository.owner.links.avatar.href,name",
                    baseUrl+"repositories/"+encodePath(orgId),
                    encodePath(repoSlug),
                    encodePath(branch)));
            if(response.getStatus() == 404){
                return null;
            }
            return om.readValue(response.getContent(), BbCloudBranch.class);
        } catch (Exception e) {
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
            InputStream inputStream = request.get(String.format("%s/%s/?fields=mainbranch.*,mainbranch.target.*,mainbranch.target.repository.*,mainbranch.target.repository.mainbranch.name,mainbranch.target.repository.owner.*,mainbranch.target.repository.owner.links.avatar.*",
                    baseUrl+"repositories/"+encodePath(orgId),
                    encodePath(repoSlug)))
                    .getContent();
            Map<String, BbCloudBranch> resp = om.readValue(inputStream, new TypeReference<Map<String, BbCloudBranch>>() {});
            return resp.get("mainbranch");
        } catch (IOException e) {
            throw handleException(e);
        }

    }

    @Override
    public boolean isEmptyRepo(@Nonnull String orgId, @Nonnull String repoSlug) {
        throw new NotImplementedException("Not implemented");
    }

    @Extension
    public static class BitbucketCloudApiFactory extends BitbucketApiFactory {
        @Override
        public boolean handles(@Nonnull String scmId) {
            return scmId.equals(BitbucketCloudScm.ID);
        }

        @Nonnull
        @Override
        public BitbucketApi create(@Nonnull String apiUrl, @Nonnull StandardUsernamePasswordCredentials credentials) {
            return new BitbucketCloudApi(apiUrl, credentials);
        }
    }

}

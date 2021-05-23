package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.endpoints.BitbucketEndpointConfiguration;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Item;
import hudson.model.User;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud.BitbucketCloudScm;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BitbucketServerScm;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.scm.AbstractScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmContentProviderParams;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFile;
import io.jenkins.blueocean.rest.model.BlueOrganization;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public abstract class AbstractBitbucketScmContentProvider extends AbstractScmContentProvider {

    @Override
    protected Object getContent(ScmGetRequest request) {
        BitbucketApi api = BitbucketServerScm.getApi(request.getApiUrl(), this.getScmId(), request.getCredentials());
        BbBranch branch=null;
        String branchName = request.getBranch();

        BbBranch defaultBranch = api.getDefaultBranch(request.getOwner(), request.getRepo());
        if(defaultBranch == null){ //empty repo
            throw new ServiceException.NotFoundException(request.getPath()+ " not found. This is empty and un-initialized repository");
        }
        if(branchName == null){
            branch = defaultBranch;
        }
        if(branchName != null){
            branch = api.getBranch(request.getOwner(), request.getRepo(), branchName);
            //Given branchName create this branch
            if(branch == null ){
                throw new ServiceException.BadRequestException("branch: "+branchName + " not found");
            }
        }

        String content = api.getContent(request.getOwner(), request.getRepo(), request.getPath(), branch.getLatestCommit());

        final GitContent gitContent =  new GitContent.Builder()
                .base64Data(Base64.getEncoder().encodeToString(content.getBytes( StandardCharsets.UTF_8)))
                .branch(request.getBranch())
                .size(content.length())
                .path(request.getPath())
                .owner(request.getOwner())
                .repo(request.getRepo())
                .name(request.getPath())
                //we use commitId as sha value - bitbucket doesn't use content sha to detect collision
                .sha(branch.getLatestCommit())
                .commitId(branch.getLatestCommit())
                .build();

        return new ScmFile<GitContent>() {
            @Override
            public GitContent getContent() {
                return gitContent;
            }
        };

    }

    @Override
    protected ScmContentProviderParams getScmParamsFromItem(Item item) {
        return new BitbucketScmParams(item);
    }

    @Override
    public Object saveContent(@NonNull StaplerRequest staplerRequest, @NonNull Item item) {
        JSONObject body;
        try {
            body = JSONObject.fromObject(IOUtils.toString(staplerRequest.getReader()));
        } catch (IOException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to read request body");
        }
        BitbucketScmSaveFileRequest scmSaveFileRequest = staplerRequest.bindJSON(BitbucketScmSaveFileRequest.class, body);
        if(scmSaveFileRequest == null){
            throw new ServiceException.BadRequestException(new ErrorMessage(400, "Failed to bind request"));
        }
        GitContent gitContent = scmSaveFileRequest.getContent();
        BitbucketScmParams scmParamsFromItem = new BitbucketScmParams(item);
        String owner = scmParamsFromItem.getOwner();
        String repo = scmParamsFromItem.getRepo();
        String commitId = StringUtils.isNotBlank(gitContent.getCommitId()) ? gitContent.getCommitId() : gitContent.getSha();
        BitbucketApi api = BitbucketServerScm.getApi(scmParamsFromItem.getApiUrl(), this.getScmId(), scmParamsFromItem.getCredentials());

        String content = new String(Base64.getDecoder().decode(gitContent.getBase64Data()), StandardCharsets.UTF_8);

        String message = gitContent.getMessage();
        if(message == null){
            message = gitContent.getPath()+" created with BlueOcean";
        }
        BbSaveContentResponse response = api.saveContent(owner,repo,gitContent.getPath(),content,
                message, gitContent.getBranch(), gitContent.getSourceBranch(), commitId);

        final GitContent respContent =  new GitContent.Builder()
                .branch(gitContent.getBranch())
                .path(gitContent.getPath())
                .owner(gitContent.getOwner())
                .repo(gitContent.getRepo())
                .sha(response.getCommitId())
                .name(gitContent.getPath())
                .commitId(response.getCommitId())
                .build();

        return new ScmFile<GitContent>() {
            @Override
            public GitContent getContent() {
                return respContent;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @CheckForNull
    protected BitbucketSCMSource getSourceFromItem(@NonNull Item item) {
        if (item instanceof MultiBranchProject) {
            List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
            if (!sources.isEmpty() && sources.get(0) instanceof BitbucketSCMSource) {
                return (BitbucketSCMSource) sources.get(0);
            }
        }
        return null;
    }

    static class BitbucketScmParams extends ScmContentProviderParams {

        public BitbucketScmParams(Item item) {
            super(item);
        }
        @Override
        protected String owner(@NonNull SCMSource scmSource) {
            if (scmSource instanceof BitbucketSCMSource) {
                BitbucketSCMSource bitbucketSCMSource = (BitbucketSCMSource) scmSource;
                return bitbucketSCMSource.getRepoOwner();
            }
            return null;
        }

        @Override
        protected String owner(@NonNull SCMNavigator scmNavigator) {
            return null;
        }

        @Override
        protected String repo(@NonNull SCMSource scmSource) {
            if (scmSource instanceof BitbucketSCMSource) {
                BitbucketSCMSource bitbucketSCMSource = (BitbucketSCMSource) scmSource;
                return bitbucketSCMSource.getRepository();
            }
            return null;
        }

        @Override
        protected String apiUrl(@NonNull SCMSource scmSource) {
            if (scmSource instanceof BitbucketSCMSource) {
                return ((BitbucketSCMSource)scmSource).getServerUrl();
            }
            return null;
        }

        @Override
        protected String apiUrl(@NonNull SCMNavigator scmNavigator) {
            return null;
        }

        @Override
        @NonNull
        protected StandardUsernamePasswordCredentials getCredentialForUser(@NonNull final Item item, @NonNull String apiUrl){
            User user = User.current();
            if(user == null){ //ensure this session has authenticated user
                throw new ServiceException.UnauthorizedException("No logged in user found");
            }

            StaplerRequest request = Stapler.getCurrentRequest();
            String scmId = request.getParameter("scmId");

            //get credential for this user
            AbstractBitbucketScm scm;
            final BlueOrganization organization = OrganizationFactory.getInstance().getContainingOrg(item);
            if(BitbucketEndpointConfiguration.normalizeServerUrl(apiUrl)
                    .startsWith(BitbucketEndpointConfiguration.normalizeServerUrl(BitbucketCloudScm.API_URL))
                    //tests might add scmId to indicate which Scm should be used to find credential
                    //We have to do this because apiUrl might be of WireMock server and not Github
                    || (StringUtils.isNotBlank(scmId) && scmId.equals(BitbucketCloudScm.ID))) {
                scm = new BitbucketCloudScm( () -> {
                    Preconditions.checkNotNull(organization);
                    return organization.getLink().rel("scm");
                } );
            }else{ //server
                scm = new BitbucketServerScm(( () -> {
                    Preconditions.checkNotNull(organization);
                    return organization.getLink().rel("scm");
                } ));
            }

            //pick up github credential from user's store
            StandardUsernamePasswordCredentials credential = scm.getCredential(BitbucketEndpointConfiguration.normalizeServerUrl(apiUrl));
            if(credential == null){
                throw new ServiceException.PreconditionRequired("Can't access content from Bitbucket: no credential found");
            }
            return credential;
        }
    }
}

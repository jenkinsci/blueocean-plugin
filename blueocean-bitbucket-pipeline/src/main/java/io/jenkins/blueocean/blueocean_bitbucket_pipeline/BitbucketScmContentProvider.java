package io.jenkins.blueocean.blueocean_bitbucket_pipeline;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.google.common.collect.ImmutableMap;
import hudson.Extension;
import hudson.model.Item;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BitbucketServerScm;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import io.jenkins.blueocean.rest.impl.pipeline.scm.AbstractScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmContentProviderParams;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFile;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Extension(ordinal = -100)
public class BitbucketScmContentProvider extends AbstractScmContentProvider {
    @Override
    protected Object getContent(ScmGetRequest request) {
        BitbucketApi api = BitbucketServerScm.getApi(request.getApiUrl(), request.getCredentials());
        BbBranch branch=null;
        String branchName = request.getBranch();

        BbBranch defaultBranch = api.getDefaultBranch(request.getOwner(), request.getRepo());
        if(branchName == null){
            if(defaultBranch == null){
                throw new ServiceException.NotFoundException("This is empty and un-instialized repository. Please initialize it");
            }
            branch = defaultBranch;
        }
        if(branchName != null){
            branch = api.getBranch(request.getOwner(), request.getRepo(), branchName);
            //Given branchName create this branch
            if(branch == null){
                //if empty and unintialized repo then report error
                //TODO: remove this error once bitbucket latest API supports creation of branch on empty repo
                if(defaultBranch == null){
                    throw new ServiceException.NotFoundException("This is empty and un-instialized repository. Please initialize it");
                }
                branch = api.createBranch(request.getOwner(), request.getRepo(), ImmutableMap.of("name", branchName,
                        "startPoint", defaultBranch.getLatestCommit(),
                        "message", "new branch created"));
            }
        }

        String content = api.getContent(request.getOwner(), request.getRepo(), request.getPath(), branch.getLatestCommit());
        try {
            final GitContent gitContent =  new GitContent.Builder()
                    .base64Data(Base64.encodeBase64String(content.getBytes("UTF-8")))
                    .branch(request.getBranch())
                    .size(content.length())
                    .path(request.getPath())
                    .owner(request.getOwner())
                    .repo(request.getRepo())
                    .name(request.getPath()).commitId(branch.getLatestCommit())
                    .build();

            return new ScmFile<GitContent>() {
                @Override
                public GitContent getContent() {
                    return gitContent;
                }
            };
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException.UnexpectedErrorException("Failed to base64 encode content: "+e.getMessage(), e);
        }
    }

    @Override
    protected ScmContentProviderParams getScmParamsFromItem(Item item) {
        return new BitbucketScmParams(item);
    }

    @Override
    public Object saveContent(@Nonnull StaplerRequest staplerRequest, @Nonnull Item item) {
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
        String commitId = gitContent.getCommitId();
        BitbucketApi api = BitbucketServerScm.getApi(scmParamsFromItem.getApiUrl(), scmParamsFromItem.getCredentials());

        String branch=gitContent.getBranch();
        BbBranch defaultBranch = api.getDefaultBranch(owner, repo);

        BbBranch commitBranch;
        if(branch == null){ //get default branch
            assertDefaultBranch(defaultBranch, gitContent);
            commitBranch = defaultBranch;
        }else{ //check if this branch doesn't exist, if it doesn't then create this branch
            commitBranch = api.getBranch(owner,repo, branch);
            if(commitBranch == null){
                if(commitId == null){
                    assertDefaultBranch(defaultBranch, gitContent);
                    commitId = defaultBranch.getLatestCommit();
                }
                commitBranch =  api.createBranch(owner,repo,ImmutableMap.of("name", branch, "startPoint", commitId, "message", "Creating new branch "+branch));
            }
        }
        branch = commitBranch.getDisplayId();
        boolean fileExists = api.fileExists(owner,repo,gitContent.getPath(),branch);
        if(!fileExists){
            commitId = null; //if file doesn't exist we need to send null commitId
        } else if(commitId == null){ // else patch commitId with whats available on default branch
            commitId = commitBranch.getLatestCommit();
        }

        String content;
        try {
            content = new String(Base64.decodeBase64(gitContent.getBase64Data()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException.UnexpectedErrorException(e.getMessage(), e);
        }
        String message = gitContent.getMessage();
        if(message == null){
            message = "Saving "+gitContent.getPath();
        }
        BbSaveContentResponse response = api.saveContent(owner,repo,gitContent.getPath(),content,
                message, branch, commitId);

        final GitContent respContent =  new GitContent.Builder()
                .branch(branch)
                .path(gitContent.getPath())
                .owner(gitContent.getOwner())
                .repo(gitContent.getRepo())
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

    private void assertDefaultBranch(BbBranch defaultBranch, GitContent gitContent){
        if(defaultBranch == null){
            throw new ServiceException.BadRequestException(
                    String.format("No default branch on project %s, repo %s. Please resubmit request with content.branch",
                    gitContent.getOwner(),gitContent.getRepo()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean support(@Nonnull Item item) {
        if (item instanceof MultiBranchProject) {
            List<SCMSource> sources = ((MultiBranchProject) item).getSCMSources();
            return (!sources.isEmpty() && sources.get(0) instanceof BitbucketSCMSource);
        }
        return false;
    }

    static class BitbucketScmParams extends ScmContentProviderParams {

        public BitbucketScmParams(Item item) {
            super(item);
        }
        @Override
        protected String owner(@Nonnull SCMSource scmSource) {
            if (scmSource instanceof BitbucketSCMSource) {
                BitbucketSCMSource bitbucketSCMSource = (BitbucketSCMSource) scmSource;
                return bitbucketSCMSource.getRepoOwner();
            }
            return null;
        }

        @Override
        protected String owner(@Nonnull SCMNavigator scmNavigator) {
            return null;
        }

        @Override
        protected String repo(@Nonnull SCMSource scmSource) {
            if (scmSource instanceof BitbucketSCMSource) {
                BitbucketSCMSource bitbucketSCMSource = (BitbucketSCMSource) scmSource;
                return bitbucketSCMSource.getRepository();
            }
            return null;
        }

        @Override
        protected String apiUrl(@Nonnull SCMSource scmSource) {
            if (scmSource instanceof BitbucketSCMSource) {
                return ((BitbucketSCMSource)scmSource).getBitbucketServerUrl();
            }
            return null;
        }

        @Override
        protected String apiUrl(@Nonnull SCMNavigator scmNavigator) {
            return null;
        }

        @Override
        protected String credentialId(@Nonnull SCMSource scmSource) {
            if (scmSource instanceof BitbucketSCMSource) {
                return ((BitbucketSCMSource)scmSource).getCredentialsId();
            }
            return null;
        }

        @Override
        protected String credentialId(@Nonnull SCMNavigator scmNavigator) {
            return null;
        }
    }
}

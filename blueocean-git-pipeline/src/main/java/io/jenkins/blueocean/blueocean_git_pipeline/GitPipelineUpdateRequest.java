package io.jenkins.blueocean.blueocean_git_pipeline;

import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.PersistedList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineUpdateRequest;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.branch.BranchSource;
import jenkins.branch.MultiBranchProject;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import org.acegisecurity.Authentication;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.Collections;

/**
 * @author Vivek Pandey
 */
public class GitPipelineUpdateRequest extends BluePipelineUpdateRequest {
    private final BlueScmConfig scmConfig;

    @DataBoundConstructor
    public GitPipelineUpdateRequest(BlueScmConfig scmConfig) {
        this.scmConfig = scmConfig;
    }

    @CheckForNull
    @Override
    @SuppressWarnings("unchecked")
    public BluePipeline update(BluePipeline pipeline) throws IOException {
        Item item = Jenkins.getInstance().getItemByFullName(pipeline.getFullName());

        if(item instanceof MultiBranchProject){
            ACL acl = Jenkins.getInstance().getACL();
            Authentication a = Jenkins.getAuthentication();
            if(!acl.hasPermission(a, Item.CONFIGURE)){
                throw new ServiceException.ForbiddenException(
                        String.format("Failed to update Git pipeline: %s. User %s doesn't have Job configure permission", pipeline.getName(), a.getName()));
            }
            MultiBranchProject mbp = (MultiBranchProject) item;

            BranchSource branchSource = getGitScmSource(mbp);
            if(branchSource != null){
                mbp.getSourcesList().replaceBy(Collections.singleton(branchSource));

                mbp.scheduleBuild2(0, new CauseAction(new Cause.UserIdCause()));
            }
        }
        return pipeline;
    }

    @SuppressWarnings("unchecked")
    private BranchSource getGitScmSource(MultiBranchProject mbp){
        String sourceUri=null;

        String credentialId=null;

        if(scmConfig != null){
            sourceUri = scmConfig.getUri();
            if(sourceUri != null) {
                GitUtils.validateCredentials(mbp, sourceUri, scmConfig.getCredentialId());
            }
            credentialId = scmConfig.getCredentialId() == null ? "" : scmConfig.getCredentialId();
        }

        PersistedList<BranchSource> sources = mbp.getSourcesList();

        for(BranchSource source:sources){
            if(source.getSource() instanceof GitSCMSource){
                GitSCMSource gitSCMSource = (GitSCMSource) source.getSource();
                String remote = gitSCMSource.getRemote();
                if(sourceUri != null
                        && !sourceUri.equals(gitSCMSource.getRemote())){
                    remote = sourceUri;
                }

                String cred = gitSCMSource.getCredentialsId();
                if(!gitSCMSource.getCredentialsId().equals(credentialId)){
                    cred = credentialId;
                }
                GitSCMSource s = new GitSCMSource(gitSCMSource.getId(), remote, cred,
                        gitSCMSource.getIncludes(),
                        gitSCMSource.getExcludes(),
                        gitSCMSource.isIgnoreOnPushNotifications());
                s.setOwner(mbp);
                return new BranchSource(s);
            }
        }
        if(sourceUri != null) { //if no scm sources in this MBP project, add a new one using passed sourceUri
            return new BranchSource(new GitSCMSource(null, sourceUri, credentialId, "*", "", false));
        }
        return null;
    }
}

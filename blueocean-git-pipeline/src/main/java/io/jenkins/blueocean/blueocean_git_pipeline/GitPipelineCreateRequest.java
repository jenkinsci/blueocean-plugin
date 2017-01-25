package io.jenkins.blueocean.blueocean_git_pipeline;

import hudson.model.Cause;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreateRequestImpl;
import jenkins.branch.BranchSource;
import jenkins.branch.MultiBranchProjectDescriptor;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public class GitPipelineCreateRequest extends AbstractPipelineCreateRequestImpl {

    private static final String MODE = "org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject";


    private BlueScmConfig scmConfig;

    @DataBoundConstructor
    public GitPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        setName(name);
        if(scmConfig == null){
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Git pipeline:"+name)
                    .add(new ErrorMessage.Error("scmConfig", ErrorMessage.Error.ErrorCodes.MISSING.toString(), "scmConfig is required")));
        }
        this.scmConfig = scmConfig;
    }

    @Override
    public BluePipeline create(Reachable parent) throws IOException {

        String sourceUri = scmConfig.getUri();

        if (sourceUri == null) {
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Git pipeline:"+getName())
                    .add(new ErrorMessage.Error("scmConfig.uri", ErrorMessage.Error.ErrorCodes.MISSING.toString(), "uri is required")));
        }

        TopLevelItem item = create(Jenkins.getInstance(), getName(), MODE, MultiBranchProjectDescriptor.class);

        if (item instanceof WorkflowMultiBranchProject) {
            WorkflowMultiBranchProject project = (WorkflowMultiBranchProject) item;
            GitUtils.validateCredentials(project, sourceUri, scmConfig.getCredentialId());

            //XXX: set credentialId to empty string if null or we get NPE later on
            String credentialId = scmConfig.getCredentialId() == null ? "" : scmConfig.getCredentialId();

            project.getSourcesList().add(new BranchSource(new GitSCMSource(null, sourceUri, credentialId, "*", "", false)));
            project.scheduleBuild(new Cause.UserIdCause());
            return new MultiBranchPipelineImpl(project);
        } else {
            try {
                item.delete(); // we don't know about this project type
            } catch (InterruptedException e) {
                throw new ServiceException.UnexpectedErrorException("Failed to delete pipeline: " + getName());
            }
        }
        return null;
    }

}

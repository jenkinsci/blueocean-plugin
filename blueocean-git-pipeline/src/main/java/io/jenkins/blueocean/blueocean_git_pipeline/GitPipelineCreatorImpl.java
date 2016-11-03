package io.jenkins.blueocean.blueocean_git_pipeline;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreatorImpl;
import jenkins.branch.BranchSource;
import jenkins.branch.MultiBranchProjectDescriptor;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.io.IOException;

/**
 * @author Vivek Pandey
 */
@Extension
public class GitPipelineCreatorImpl extends AbstractPipelineCreatorImpl {

    private static final String CREATOR_ID = GitPipelineCreatorImpl.class.getName();
    private static final String MODE="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject";



    @Override
    public String getId() {
        return CREATOR_ID;
    }

    @Override
    public BluePipeline create(BluePipelineCreateRequest request, Reachable parent) throws IOException {

        String sourceUri = request.getScmConfig().getUri();

        if(sourceUri == null){
            throw new ServiceException.BadRequestExpception("sourceUri is required");
        }

        //XXX: set credentialId to empty string if null or we get NPE later on
        String credentialId = request.getScmConfig().getCredentialId() == null ? "" : request.getScmConfig().getCredentialId();

        TopLevelItem item = create(Jenkins.getInstance(), request.getName(), MODE, MultiBranchProjectDescriptor.class);

        if(item instanceof WorkflowMultiBranchProject){
            WorkflowMultiBranchProject project = (WorkflowMultiBranchProject) item;
            project.getSourcesList().add(new BranchSource(new GitSCMSource(null, sourceUri, credentialId, "*", "", false)));
            project.scheduleBuild(new Cause.UserIdCause());
            return new MultiBranchPipelineImpl(project);
        }else{
            try {
                item.delete(); // we don't know about this project type
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}

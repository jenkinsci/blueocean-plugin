package io.jenkins.blueocean.blueocean_git_pipeline;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.rest.model.BluePipelineCreator;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreateRequestImpl;
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
public class GitPipelineCreatorImpl extends BluePipelineCreator {

    private static final String CREATOR_ID = GitPipelineCreatorImpl.class.getName();

    @Override
    public String getId() {
        return CREATOR_ID;
    }

    @Override
    public Class<? extends BluePipelineCreateRequest> getType() {
        return RequestImpl.class;
    }


    public static class RequestImpl extends AbstractPipelineCreateRequestImpl{
        private static final String MODE="org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject";


        private String name;

        private BlueScmConfig scmConfig;

        @Override
        public void setName(String name) {
            this.name = name;
        }

        public BlueScmConfig getScmConfig() {
            return scmConfig;
        }

        public void setScmConfig(BlueScmConfig scmConfig) {
            this.scmConfig = scmConfig;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public BluePipeline create(Reachable parent) throws IOException{

            String sourceUri = scmConfig.getUri();

            if(sourceUri == null){
                throw new ServiceException.BadRequestExpception("uri is required");
            }

            //XXX: set credentialId to empty string if null or we get NPE later on
            String credentialId = scmConfig.getCredentialId() == null ? "" : scmConfig.getCredentialId();

            TopLevelItem item = create(Jenkins.getInstance(), name, MODE, MultiBranchProjectDescriptor.class);

            if (item instanceof WorkflowMultiBranchProject) {
                WorkflowMultiBranchProject project = (WorkflowMultiBranchProject) item;
                project.getSourcesList().add(new BranchSource(new GitSCMSource(null, sourceUri, credentialId, "*", "", false)));
                project.scheduleBuild(new Cause.UserIdCause());
                return new MultiBranchPipelineImpl(project);
            } else {
                try {
                    item.delete(); // we don't know about this project type
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

}

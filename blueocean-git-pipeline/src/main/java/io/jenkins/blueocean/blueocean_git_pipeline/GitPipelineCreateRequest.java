package io.jenkins.blueocean.blueocean_git_pipeline;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.google.common.collect.ImmutableList;
import hudson.model.Cause;
import hudson.model.TopLevelItem;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreateRequestImpl;
import java.util.Collections;
import jenkins.branch.BranchSource;
import jenkins.branch.MultiBranchProjectDescriptor;
import jenkins.model.Jenkins;
import jenkins.plugins.git.GitSCMSource;
import org.apache.commons.lang3.StringUtils;
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
        User authenticatedUser =  User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("Must login to create a pipeline");
        }

        String sourceUri = scmConfig.getUri();

        if (sourceUri == null) {
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Git pipeline:"+getName())
                    .add(new ErrorMessage.Error("scmConfig.uri", ErrorMessage.Error.ErrorCodes.MISSING.toString(), "uri is required")));
        }

        TopLevelItem item = create(Jenkins.getInstance(), getName(), MODE, MultiBranchProjectDescriptor.class);

        if (item instanceof WorkflowMultiBranchProject) {
            WorkflowMultiBranchProject project = (WorkflowMultiBranchProject) item;

            if(StringUtils.isNotBlank(scmConfig.getCredentialId())) {
                Domain domain = CredentialsUtils.findDomain(scmConfig.getCredentialId(), authenticatedUser);
                if(domain == null){
                    throw new ServiceException.BadRequestExpception(
                            new ErrorMessage(400, "Failed to create pipeline")
                                    .add(new ErrorMessage.Error("scm.credentialId",
                                            ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                                            "No domain in user credentials found for credentialId: "+ scmConfig.getCredentialId())));
                }
                project.addProperty(
                        new BlueOceanCredentialsProvider.FolderPropertyImpl(authenticatedUser.getId(),
                                scmConfig.getCredentialId(), new Domain("blue-ocean-proxy", "Blue Ocean Proxy domain", /** TODO insert specification here **/
                            Collections.<DomainSpecification>emptyList())));
            }

            String credentialId = StringUtils.defaultString(scmConfig.getCredentialId());

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

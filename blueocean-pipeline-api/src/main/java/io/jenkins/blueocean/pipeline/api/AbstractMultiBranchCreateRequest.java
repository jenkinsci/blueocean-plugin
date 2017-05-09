package io.jenkins.blueocean.pipeline.api;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.Lists;
import hudson.model.Cause;
import hudson.model.Failure;
import hudson.model.TopLevelItem;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ErrorMessage.Error;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.OrganizationResolver;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
import io.jenkins.blueocean.rest.impl.pipeline.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.branch.BranchSource;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.MultiBranchProjectDescriptor;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSource;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Creates {@link MultiBranchProject}s with a single {@link SCMSource}
 */
public abstract class AbstractMultiBranchCreateRequest extends AbstractPipelineCreateRequest {

    private static final String DESCRIPTOR_NAME = "org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject";

    public AbstractMultiBranchCreateRequest(String name, BlueScmConfig scmConfig) {
        super(name, scmConfig);
    }

    @Override
    @SuppressWarnings("unchecked")
    public BluePipeline create(Reachable parent) throws IOException {
        validateInternal(getName(), scmConfig);
        MultiBranchProject project = createMultiBranchProject();
        assignCredentialToProject(scmConfig, project);
        SCMSource source = createSource(project, scmConfig);
        project.getSourcesList().add(new BranchSource(source));
        project.save();
        project.scheduleBuild(new Cause.UserIdCause());
        return BluePipelineFactory.getPipelineInstance(project, OrganizationResolver.getInstance().getContainingOrg(project.getItemGroup()));
    }

    /**
     * Create the source for the MultiBranchProject created with this request
     * @param project that was created
     * @param scmConfig config
     * @return valid SCMSource
     */
    protected abstract SCMSource createSource(@Nonnull MultiBranchProject project, @Nonnull BlueScmConfig scmConfig);

    /**
     * Validate the provided SCMConfig and test that a connection can be made to the SCM server
     * @param name of pipeline being created
     * @param scmConfig to validate
     * @return errors occuring during validation
     */
    protected abstract List<Error> validate(String name, BlueScmConfig scmConfig);

    private MultiBranchProject createMultiBranchProject() throws IOException {
        TopLevelItem item = createProject(getName(), DESCRIPTOR_NAME, MultiBranchProjectDescriptor.class);
        if (!(item instanceof WorkflowMultiBranchProject)) {
            try {
                item.delete(); // we don't know about this project type
            } catch (InterruptedException e) {
                throw new ServiceException.UnexpectedErrorException("Failed to delete pipeline: " + getName());
            }
        }
        return (MultiBranchProject) item;
    }

    private void assignCredentialToProject(BlueScmConfig scmConfig, MultiBranchProject project) throws IOException {
        User authenticatedUser = User.current();
        if(StringUtils.isNotBlank(scmConfig.getCredentialId())) {
            Domain domain = CredentialsUtils.findDomain(scmConfig.getCredentialId(), authenticatedUser);
            if(domain == null){
                throw new ServiceException.BadRequestExpception(
                    new ErrorMessage(400, "Failed to create pipeline")
                        .add(new ErrorMessage.Error("scm.credentialId",
                            ErrorMessage.Error.ErrorCodes.INVALID.toString(),
                            "No domain in user credentials found for credentialId: "+ scmConfig.getCredentialId())));
            }
            if (StringUtils.isEmpty(scmConfig.getUri())) {
                throw new ServiceException.BadRequestExpception("uri not specified");
            }
            if(domain.test(new BlueOceanDomainRequirement())) { //this is blueocean specific domain
                project.addProperty(
                    new BlueOceanCredentialsProvider.FolderPropertyImpl(authenticatedUser.getId(),
                        scmConfig.getCredentialId(),
                        BlueOceanCredentialsProvider.createDomain(scmConfig.getUri())));
            }
        }
    }

    private void validateInternal(String name, BlueScmConfig scmConfig) {
        User authenticatedUser =  User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("Must login to create a pipeline");
        }

        if(scmConfig == null || scmConfig.getUri() == null){
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create pipeline")
                .add(new ErrorMessage.Error("scmConfig", ErrorMessage.Error.ErrorCodes.MISSING.toString(), "scmConfig is required")));
        }

        List<Error> errors = Lists.newLinkedList(validate(name, scmConfig));

        try {
            Jenkins.getInstance().getProjectNamingStrategy().checkName(getName());
        }catch (Failure f){
            errors.add(new ErrorMessage.Error("scmConfig.name", ErrorMessage.Error.ErrorCodes.INVALID.toString(), name + "in not a valid name"));
        }

        if(Jenkins.getInstance().getItem(name)!=null) {
            errors.add(new ErrorMessage.Error("name",
                ErrorMessage.Error.ErrorCodes.ALREADY_EXISTS.toString(), name + " already exists"));
        }

        if(!errors.isEmpty()){
            throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Git pipeline:"+name).addAll(errors));
        }
    }
}

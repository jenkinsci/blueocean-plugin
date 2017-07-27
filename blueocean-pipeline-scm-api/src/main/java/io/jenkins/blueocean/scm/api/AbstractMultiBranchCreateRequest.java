package io.jenkins.blueocean.scm.api;

import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.Lists;
import hudson.model.Cause;
import hudson.model.Failure;
import hudson.model.TopLevelItem;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ErrorMessage.Error;
import io.jenkins.blueocean.commons.ErrorMessage.Error.ErrorCodes;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.credential.CredentialsUtils;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.factory.BluePipelineFactory;
import io.jenkins.blueocean.rest.factory.organization.OrganizationFactory;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanDomainRequirement;
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
import java.util.Arrays;
import java.util.List;

/**
 * Creates {@link MultiBranchProject}s with a single {@link SCMSource}
 */
public abstract class AbstractMultiBranchCreateRequest extends AbstractPipelineCreateRequest {

    private static final String DESCRIPTOR_NAME = "org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject";

    private static final String ERROR_FIELD_SCM_CONFIG_URI = "scmConfig.uri";
    private static final String ERROR_FIELD_SCM_CONFIG_NAME = "scmConfig.name";
    private static final String ERROR_NAME = "name";
    private static final String ERROR_FIELD_SCM_CREDENTIAL_ID = "scm.credentialId";

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
        return BluePipelineFactory.getPipelineInstance(project, OrganizationFactory.getInstance().getContainingOrg(project.getItemGroup()));
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
                throw new ServiceException.BadRequestException(
                    new ErrorMessage(400, "Failed to create pipeline")
                        .add(new Error(ERROR_FIELD_SCM_CREDENTIAL_ID,
                            Error.ErrorCodes.INVALID.toString(),
                            "No domain in user credentials found for credentialId: "+ scmConfig.getCredentialId())));
            }
            if (StringUtils.isEmpty(scmConfig.getUri())) {
                throw new ServiceException.BadRequestException("uri not specified");
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

        checkUserIsAuthenticatedAndHasItemCreatePermission();

        // If scmConfig is empty then we are missing the uri and name
        if (scmConfig == null) {
            throw fail(new Error("scmConfig", ErrorCodes.MISSING.toString(), "scmConfig is required"));
        }

        if (scmConfig.getUri() == null) {
            throw fail(new Error(ERROR_FIELD_SCM_CONFIG_URI, ErrorCodes.MISSING.toString(), ERROR_FIELD_SCM_CONFIG_URI + "is required"));
        }

        if (getName() == null) {
            throw fail(new Error(ERROR_FIELD_SCM_CONFIG_NAME, ErrorCodes.MISSING.toString(), ERROR_FIELD_SCM_CONFIG_NAME + " is required"));
        }

        List<Error> errors = Lists.newLinkedList(validate(name, scmConfig));

        // Validate that name matches rules
        try {
            Jenkins.getInstance().getProjectNamingStrategy().checkName(getName());
        }catch (Failure f){
            errors.add(new Error(ERROR_FIELD_SCM_CONFIG_NAME, Error.ErrorCodes.INVALID.toString(),  getName() + " in not a valid name"));
        }

        if(getParent().getItem(name)!=null) {
            errors.add(new Error(ERROR_NAME, Error.ErrorCodes.ALREADY_EXISTS.toString(), getName() + " already exists"));
        }

        if(!errors.isEmpty()){
            throw fail(errors);
        }
    }

    private static ServiceException fail(List<Error> errors) {
        ErrorMessage errorMessage = new ErrorMessage(400, "Failed to create pipeline");
        errorMessage.addAll(errors);
        return new ServiceException.BadRequestException(errorMessage);
    }

    private static ServiceException fail(Error... errors) {
        return fail(Arrays.asList(errors));
    }
}

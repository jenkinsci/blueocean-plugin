package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.plugins.credentials.Credentials;
import hudson.model.Cause;
import hudson.model.TopLevelItem;
import hudson.model.User;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreateRequestImpl;
import jenkins.branch.CustomOrganizationFolderDescriptor;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GithubPipelineCreateRequest extends AbstractPipelineCreateRequestImpl {

    private static final String DESCRIPTOR = "jenkins.branch.OrganizationFolder.org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator";
    private static final Logger logger = LoggerFactory.getLogger(GithubPipelineCreateRequest.class);

    private BlueScmConfig scmConfig;

    @DataBoundConstructor
    public GithubPipelineCreateRequest(String name, BlueScmConfig scmConfig) {
        setName(name);
        this.scmConfig = scmConfig;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BluePipeline create(Reachable parent) throws IOException {

        String apiUrl = null;
        String orgName = getName(); //default
        String credentialId = null;
        StringBuilder sb = new StringBuilder();

        if (scmConfig != null) {
            apiUrl = scmConfig.getUri();
            if (scmConfig.getConfig().get("orgName") instanceof String) {
                orgName = (String) scmConfig.getConfig().get("orgName");
            }
            credentialId = scmConfig.getCredentialId();
            if (scmConfig != null && scmConfig.getConfig().get("repos") instanceof List) {
                for (String r : (List<String>) scmConfig.getConfig().get("repos")) {
                    sb.append(String.format("(%s\\b)?", r));
                }
            }
        }

        User authenticatedUser =  User.current();
        if(authenticatedUser == null){
            throw new ServiceException.UnauthorizedException("Must login to create a pipeline");
        }

        TopLevelItem item = null;
        try {

            item = create(Jenkins.getInstance(), getName(), DESCRIPTOR, CustomOrganizationFolderDescriptor.class);

            if (item instanceof OrganizationFolder) {
                if(credentialId != null) {
                    validateCredentialId(credentialId, (AbstractFolder) item);
                    ((OrganizationFolder) item)
                            .addProperty(new BlueOceanCredentialsProvider.FolderPropertyImpl(authenticatedUser.getId(), credentialId));
                }
                GitHubSCMNavigator gitHubSCMNavigator = new GitHubSCMNavigator(apiUrl, orgName, credentialId, credentialId);
                if (sb.length() > 0) {
                    gitHubSCMNavigator.setPattern(sb.toString());
                }

                // cick of github scan build
                OrganizationFolder organizationFolder = (OrganizationFolder) item;
                organizationFolder.getNavigators().replace(gitHubSCMNavigator);
                organizationFolder.scheduleBuild(new Cause.UserIdCause());
                return new GithubOrganizationFolder(organizationFolder, parent.getLink());
            }
        }catch (Exception e){
            if(e instanceof ServiceException){
                throw e;
            }
            String msg = String.format("Error creating pipeline %s: %s",getName(),e.getMessage());
            logger.error(msg, e);
            if(item != null) {
                try {
                    item.delete();
                } catch (InterruptedException e1) {
                    logger.error(String.format("Error creating pipeline %s: %s",getName(),e1.getMessage()), e1);
                    throw new ServiceException.UnexpectedErrorException("Error cleaning up pipeline " + getName() + " due to error: " + e.getMessage(), e);

                }
            }
            throw new ServiceException.UnexpectedErrorException(msg, e);
        }
        return null;
    }

     static void validateCredentialId(String credentialId, AbstractFolder item) throws IOException {
        if (credentialId != null && !credentialId.trim().isEmpty()) {
            Credentials credentials = GithubScm.findUsernamePasswordCredential(credentialId);
            if (credentials == null) {
                try {
                    item.delete();
                } catch (InterruptedException e) {
                    throw new ServiceException.UnexpectedErrorException("Invalid credentialId: " + credentialId + ". Failure during cleaing up folder: " + item.getName() + ". Error: " + e.getMessage(), e);
                }
                throw new ServiceException.BadRequestExpception(new ErrorMessage(400, "Failed to create Git pipeline")
                        .add(new ErrorMessage.Error("credentialId", ErrorMessage.Error.ErrorCodes.INVALID.toString(), "Invalid credentialId")));

            }
        }
    }
}

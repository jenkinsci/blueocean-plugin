package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.model.Cause;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.commons.ErrorMessage;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreateRequestImpl;
import jenkins.branch.CustomOrganizationFolderDescriptor;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.Connector;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GithubPipelineCreateRequest extends AbstractPipelineCreateRequestImpl {

    private static final String DESCRIPTOR = "jenkins.branch.OrganizationFolder.org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator";

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
            apiUrl = scmConfig.getUri() != null ?  scmConfig.getUri() : GithubScm.DEFAULT_API_URI;
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
        TopLevelItem item = create(Jenkins.getInstance(), getName(), DESCRIPTOR, CustomOrganizationFolderDescriptor.class);

        if (item instanceof OrganizationFolder) {
            GitHubSCMNavigator gitHubSCMNavigator = new GitHubSCMNavigator(apiUrl, orgName, credentialId, credentialId);
            if (sb.length() > 0) {
                gitHubSCMNavigator.setPattern(sb.toString());
            }
            validateCredentialId(credentialId, (OrganizationFolder) item, gitHubSCMNavigator);

            // cick of github scan build
            OrganizationFolder organizationFolder = (OrganizationFolder) item;
            organizationFolder.getNavigators().replace(gitHubSCMNavigator);
            organizationFolder.scheduleBuild(new Cause.UserIdCause());
            return new GithubOrganizationFolder(organizationFolder, parent.getLink());
        }
        return null;
    }

     static void validateCredentialId(String credentialId, OrganizationFolder item, GitHubSCMNavigator navigator) throws IOException {
        if (credentialId != null && !credentialId.trim().isEmpty()) {
            StandardCredentials credentials = Connector.lookupScanCredentials(item, navigator.getApiUri(), credentialId);
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

package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.model.Cause;
import hudson.model.Item;
import hudson.security.ACL;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineUpdateRequest;
import io.jenkins.blueocean.rest.model.BlueScmConfig;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMNavigator;
import org.acegisecurity.Authentication;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class GithubPipelineUpdateRequest extends BluePipelineUpdateRequest {
    private final BlueScmConfig scmConfig;

    @DataBoundConstructor
    public GithubPipelineUpdateRequest(BlueScmConfig scmConfig) {
        this.scmConfig = scmConfig;
    }

    @Nonnull
    @Override
    public BluePipeline update(BluePipeline pipeline) throws IOException {
        ACL acl = Jenkins.getInstance().getACL();
        Authentication a = Jenkins.getAuthentication();
        if(!acl.hasPermission(a, Item.CONFIGURE)){
            throw new ServiceException.ForbiddenException(
                    String.format("Failed to update Git pipeline: %s. User %s doesn't have Job configure permission", pipeline.getName(), a.getName()));
        }

        Item item = Jenkins.getInstance().getItemByFullName(pipeline.getFullName());
        if(item instanceof OrganizationFolder){
            OrganizationFolder folder = (OrganizationFolder) item;

            GitHubSCMNavigator gitHubSCMNavigator = getNavigator(folder);

            if(gitHubSCMNavigator != null){
                folder.getNavigators().replace(gitHubSCMNavigator);
                folder.scheduleBuild(new Cause.UserIdCause());
            }
            return pipeline;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private GitHubSCMNavigator getNavigator(OrganizationFolder folder) throws IOException {
        String apiUrl = null;
        String credentialId = null;
        String orgName = null;
        StringBuilder sb = new StringBuilder();

        if (scmConfig != null) {
            apiUrl = scmConfig.getUri();
            credentialId = scmConfig.getCredentialId();
            if (scmConfig.getConfig().get("orgName") instanceof String) {
                orgName = (String) scmConfig.getConfig().get("orgName");
            }
        }
        for(SCMNavigator navigator: folder.getNavigators()){
            if(navigator instanceof GitHubSCMNavigator){
                GitHubSCMNavigator scmNavigator = (GitHubSCMNavigator) navigator;
                if(scmNavigator.getApiUri()!= null && !scmNavigator.getApiUri().equals(apiUrl)){
                    apiUrl = scmNavigator.getApiUri();
                }
                if(scmNavigator.getScanCredentialsId() != null && !scmNavigator.getScanCredentialsId().equals(credentialId)){
                    credentialId = scmNavigator.getScanCredentialsId();
                }
                if(!scmNavigator.getRepoOwner().equals(orgName)){
                    orgName = scmNavigator.getRepoOwner();
                }
                GitHubSCMNavigator gitHubSCMNavigator = new GitHubSCMNavigator(apiUrl, orgName, credentialId, credentialId);

                GithubPipelineCreateRequest.validateCredentialId(credentialId, folder);

                if (scmConfig != null && scmConfig.getConfig().get("repos") instanceof List) {
                    for (String r : (List<String>) scmConfig.getConfig().get("repos")) {
                        sb.append(String.format("(%s\\b)?", r));
                    }
                }

                if (sb.length() > 0) {
                    gitHubSCMNavigator.setPattern(sb.toString());
                }

                return gitHubSCMNavigator;
            }
        }
        return null;
    }
}

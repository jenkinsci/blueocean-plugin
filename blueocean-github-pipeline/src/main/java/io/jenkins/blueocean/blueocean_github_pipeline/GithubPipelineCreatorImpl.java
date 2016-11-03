package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderPipelineImpl;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreatorImpl;
import jenkins.branch.CustomOrganizationFolderDescriptor;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

import java.io.IOException;
import java.util.List;

/**
 * @author Vivek Pandey
 */
@Extension
public class GithubPipelineCreatorImpl extends AbstractPipelineCreatorImpl {
    private static final String DESCRIPTOR = "jenkins.branch.OrganizationFolder.org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator";

    private static final String CREATOR_ID = GithubPipelineCreatorImpl.class.getName();

    @Override
    public String getId() {
        return CREATOR_ID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BluePipeline create(BluePipelineCreateRequest request, Reachable parent) throws IOException {

        String apiUrl = request.getScmConfig().getUri();

        String orgName = null; //default
        if(request.getScmConfig().getConfig().get("orgName") instanceof String){
            orgName = (String) request.getScmConfig().getConfig().get("orgName");
        }

        String credentialId = request.getScmConfig().getCredentialId();


        GitHubSCMNavigator gitHubSCMNavigator = new GitHubSCMNavigator(apiUrl, orgName, credentialId, credentialId);
        StringBuilder sb = new StringBuilder();
        if(request.getScmConfig().getConfig().get("repos") instanceof List) {
            for (String r : (List<String>)request.getScmConfig().getConfig().get("repos")) {
                sb.append(String.format("(%s\\b)?", r));
            }
        }

        if(sb.length() > 0){
            gitHubSCMNavigator.setPattern(sb.toString());
        }

        TopLevelItem item = create(Jenkins.getInstance(),request.getName(), DESCRIPTOR, CustomOrganizationFolderDescriptor.class);

        if(item instanceof OrganizationFolder){
            OrganizationFolder organizationFolder = (OrganizationFolder) item;
            organizationFolder.getNavigators().replace(gitHubSCMNavigator);
            organizationFolder.scheduleBuild(new Cause.UserIdCause());
            return new OrganizationFolderPipelineImpl(organizationFolder, parent.getLink());
        }
        return null;
    }
}

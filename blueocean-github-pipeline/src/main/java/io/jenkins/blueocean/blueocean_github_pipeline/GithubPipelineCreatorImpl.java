package io.jenkins.blueocean.blueocean_github_pipeline;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.TopLevelItemDescriptor;
import hudson.security.ACL;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderPipelineImpl;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.rest.model.BluePipelineCreator;
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
public class GithubPipelineCreatorImpl extends BluePipelineCreator {
    private static final String NAVIGATOR = "jenkins.branch.OrganizationFolder.org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator";

    private static final String CREATOR_ID = GithubPipelineCreatorImpl.class.getName();

    @Override
    public String getId() {
        return CREATOR_ID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BluePipeline create(BluePipelineCreateRequest request, Reachable parent) throws IOException {
        ACL acl = Jenkins.getInstance().getACL();
        acl.checkPermission(Item.CREATE);
        TopLevelItemDescriptor descriptor = Items.all().findByName(NAVIGATOR);
        if(descriptor == null || !(descriptor instanceof CustomOrganizationFolderDescriptor)){
            return null;
        }
        ItemGroup p = Jenkins.getInstance();
        descriptor.checkApplicableIn(p);

        acl.checkCreatePermission(p, descriptor);
        Item item = Jenkins.getInstance().createProject(descriptor, request.getName(), true);


        String apiUrl = null;
        if(request.getConfig().get("apiUrl") != null && request.getConfig().get("apiUrl") instanceof String){
            apiUrl = (String) request.getConfig().get("apiUrl");
        }

        String orgName = request.getName(); //default
        if(request.getConfig().get("orgName") != null && request.getConfig().get("orgName") instanceof String){
            orgName = (String) request.getConfig().get("orgName");
        }

        String credentialId = null;
        if(request.getConfig().get("credentialId") != null && request.getConfig().get("credentialId") instanceof String){
            credentialId = (String) request.getConfig().get("credentialId");
        }

        GitHubSCMNavigator gitHubSCMNavigator = new GitHubSCMNavigator(apiUrl, orgName, credentialId, credentialId);
        StringBuilder sb = new StringBuilder();
        if(request.getConfig().get("repos") instanceof List) {
            for (String r : (List<String>)request.getConfig().get("repos")) {
                sb.append(String.format("(%s\\b)?", r));
            }
        }

        if(sb.length() > 0){
            gitHubSCMNavigator.setPattern(sb.toString());
        }

        if(item instanceof OrganizationFolder){
            OrganizationFolder organizationFolder = (OrganizationFolder) item;
            organizationFolder.getNavigators().replace(gitHubSCMNavigator);
            organizationFolder.scheduleBuild(new Cause.UserIdCause());
            return new OrganizationFolderPipelineImpl(organizationFolder, parent.getLink());
        }
        return null;
    }
}

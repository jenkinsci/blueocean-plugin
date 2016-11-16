package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.TopLevelItem;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.Reachable;
import io.jenkins.blueocean.rest.impl.pipeline.OrganizationFolderPipelineImpl;
import io.jenkins.blueocean.rest.model.BluePipeline;
import io.jenkins.blueocean.rest.model.BluePipelineCreateRequest;
import io.jenkins.blueocean.service.embedded.rest.AbstractPipelineCreatorImpl;
import jenkins.branch.CustomOrganizationFolderDescriptor;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMSourceOwner;
import org.jenkinsci.plugins.github_branch_source.Connector;
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

        String apiUrl=null;
        if(request.getScmConfig() != null) {
            apiUrl = request.getScmConfig().getUri();
        }

        String orgName = request.getName(); //default
        if(request.getScmConfig().getConfig().get("orgName") instanceof String){
            orgName = (String) request.getScmConfig().getConfig().get("orgName");
        }

        String credentialId = request.getScmConfig().getCredentialId();

        TopLevelItem item = create(Jenkins.getInstance(),request.getName(), DESCRIPTOR, CustomOrganizationFolderDescriptor.class);

        if(item instanceof OrganizationFolder){
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

            validateCredentialId(credentialId, (OrganizationFolder) item, gitHubSCMNavigator);

            // cick of github scan build
            OrganizationFolder organizationFolder = (OrganizationFolder) item;
            organizationFolder.getNavigators().replace(gitHubSCMNavigator);
            organizationFolder.scheduleBuild(new Cause.UserIdCause());
            return new OrganizationFolderPipelineImpl(organizationFolder, parent.getLink());
        }
        return null;
    }

    private void validateCredentialId(String credentialId, OrganizationFolder item, GitHubSCMNavigator navigator) throws IOException {
        StandardCredentials credentials = Connector.lookupScanCredentials((SCMSourceOwner) item, navigator.getApiUri(), credentialId);
        if(credentials == null){
            try {
                item.delete();
            } catch (InterruptedException e) {
                throw new ServiceException.UnexpectedErrorException("Invalid credentialId: "+credentialId+". Failure during cleaing up folder: "+item.getName() + ". Error: "+e.getMessage(), e);
            }
            throw new ServiceException.BadRequestExpception("Invalid credentialId: "+credentialId);
        }
    }

}

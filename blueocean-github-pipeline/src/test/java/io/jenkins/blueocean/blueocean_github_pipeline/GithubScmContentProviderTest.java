package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.util.DescribableList;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Stapler.class, OrganizationFolder.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*"})
public class GithubScmContentProviderTest extends GithubMockBase{

    @Test
    public void getContentForOrgFolder() throws UnirestException {
        String credentialId = CreateGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        OrganizationFolder orgFolder = mockOrgFolder(credentialId);

        GithubFile content = (GithubFile) new GithubScmContentProvider().getContent(staplerRequest, orgFolder);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", content.getContent().getSha());
        assertEquals("PR-demo", content.getContent().getRepo());
        assertEquals("cloudbeers", content.getContent().getOwner());
    }

    @Test
    public void getContentForMbp() throws UnirestException {
        String credentialId = CreateGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        OrganizationFolder organizationFolder = mockOrgFolder(credentialId);

        MultiBranchProject mbp = mockMbp(organizationFolder, credentialId);

        GithubFile content = (GithubFile) new GithubScmContentProvider().getContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", content.getContent().getSha());
        assertEquals("PR-demo", content.getContent().getRepo());
        assertEquals("cloudbeers", content.getContent().getOwner());
    }

    private StaplerRequest mockStapler(){
        mockStatic(Stapler.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        when(Stapler.getCurrentRequest()).thenReturn(staplerRequest);
        when(staplerRequest.getRequestURI()).thenReturn("http://localhost:8080/jenkins/blue/rest/");
        when(staplerRequest.getParameter("path")).thenReturn("Jenkinsfile");
        when(staplerRequest.getParameter("repo")).thenReturn("PR-demo");
        return staplerRequest;
    }

    private OrganizationFolder mockOrgFolder(String credentialId){

        OrganizationFolder orgFolder = mock(OrganizationFolder.class);

        //mock GithubSCMNavigator
        GitHubSCMNavigator navigator = mock(GitHubSCMNavigator.class);
        when(navigator.getApiUri()).thenReturn(githubApiUrl);
        when(navigator.getScanCredentialsId()).thenReturn(credentialId);
        when(navigator.getRepoOwner()).thenReturn("cloudbeers");


        when((orgFolder).getSCMNavigators()).thenReturn(Lists.<SCMNavigator>newArrayList(navigator));

        //mock blueocean credential provider stuff
        BlueOceanCredentialsProvider.FolderPropertyImpl folderProperty = mock(BlueOceanCredentialsProvider.FolderPropertyImpl.class);
        DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> properties = new DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor>(orgFolder);
        properties.add(new BlueOceanCredentialsProvider.FolderPropertyImpl(
                user.getId(), credentialId,
                BlueOceanCredentialsProvider.createDomain(githubApiUrl)
        ));
        when(orgFolder.getProperties()).thenReturn(properties);
        Domain domain = mock(Domain.class);
        when(domain.getName()).thenReturn(GithubScm.DOMAIN_NAME);
        when(folderProperty.getDomain()).thenReturn(domain);
        return orgFolder;
    }

    private MultiBranchProject mockMbp(OrganizationFolder orgFolder, String credentialId){
        MultiBranchProject mbp = mock(MultiBranchProject.class);
        when(mbp.getName()).thenReturn("PR-demo");
        when(mbp.getParent()).thenReturn(orgFolder);
        GitHubSCMSource scmSource = mock(GitHubSCMSource.class);
        when(scmSource.getApiUri()).thenReturn(githubApiUrl);
        when(scmSource.getScanCredentialsId()).thenReturn(credentialId);
        when(scmSource.getRepoOwner()).thenReturn("cloudbeers");
        when(scmSource.getRepository()).thenReturn("PR-demo");
        when(mbp.getSCMSources()).thenReturn(Lists.<SCMSource>newArrayList(scmSource));
        DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> mbpProperties = new DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor>(orgFolder);
        when(mbp.getProperties()).thenReturn(mbpProperties);
        return mbp;
    }


}

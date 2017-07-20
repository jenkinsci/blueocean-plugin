package io.jenkins.blueocean.blueocean_github_pipeline;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import hudson.tasks.Mailer;
import hudson.util.DescribableList;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import jenkins.branch.MultiBranchProject;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMNavigator;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.junit.Test;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Vivek Pandey
 */
public class GithubScmContentProviderTest extends GithubMockBase{

    @Test
    public void getContentForOrgFolder() throws UnirestException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        OrganizationFolder orgFolder = mockOrgFolder(user, credentialId);

        GithubFile content = (GithubFile) new GithubScmContentProvider().getContent(staplerRequest, orgFolder);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", content.getContent().getSha());
        assertEquals("PR-demo", content.getContent().getRepo());
        assertEquals("cloudbeers", content.getContent().getOwner());
    }


    @Test
    public void getContentForOrgFolderGHE() throws UnirestException {
        String credentialId = createGithubEnterpriseCredential();

        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        OrganizationFolder orgFolder = mockOrgFolder(user, credentialId);

        GithubFile content = (GithubFile) new GithubScmContentProvider().getContent(staplerRequest, orgFolder);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", content.getContent().getSha());
        assertEquals("PR-demo", content.getContent().getRepo());
        assertEquals("cloudbeers", content.getContent().getOwner());
    }

    @Test
    public void getContentForMbp() throws UnirestException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        OrganizationFolder organizationFolder = mockOrgFolder(user, credentialId);

        MultiBranchProject mbp = mockMbp(organizationFolder, credentialId);

        GithubFile content = (GithubFile) new GithubScmContentProvider().getContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", content.getContent().getSha());
        assertEquals("PR-demo", content.getContent().getRepo());
        assertEquals("cloudbeers", content.getContent().getOwner());
    }

    @Test
    public void getContentForMbpGHE() throws UnirestException {
        String credentialId = createGithubEnterpriseCredential();

        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        OrganizationFolder organizationFolder = mockOrgFolder(user, credentialId);

        MultiBranchProject mbp = mockMbp(organizationFolder, credentialId);

        GithubFile content = (GithubFile) new GithubScmContentProvider().getContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", content.getContent().getSha());
        assertEquals("PR-demo", content.getContent().getRepo());
        assertEquals("cloudbeers", content.getContent().getOwner());
    }


    @Test
    public void unauthorizedAccessToContentForOrgFolderShouldFail() throws UnirestException, IOException {
        User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createGithubCredential(alice);

        StaplerRequest staplerRequest = mockStapler();

        OrganizationFolder orgFolder = mockOrgFolder(user, aliceCredentialId);

        try {
            //Bob trying to access content but his credential is not setup so should fail
            new GithubScmContentProvider().getContent(staplerRequest, orgFolder);
        }catch (ServiceException.PreconditionRequired e){
            assertEquals("Can't access content from github: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }

    @Test
    public void unauthorizedAccessToContentForOrgFolderGHEShouldFail() throws UnirestException, IOException {
        User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createGithubEnterpriseCredential(alice);

        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        OrganizationFolder orgFolder = mockOrgFolder(user, aliceCredentialId);

        try {
            //Bob trying to access content but his credential is not setup so should fail
            new GithubScmContentProvider().getContent(staplerRequest, orgFolder);
        }catch (ServiceException.PreconditionRequired e){
            assertEquals("Can't access content from github: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }

    @Test
    public void unauthorizedAccessToContentForMbpShouldFail() throws UnirestException, IOException {
        User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createGithubCredential(alice);

        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        OrganizationFolder organizationFolder = mockOrgFolder(alice, aliceCredentialId);

        MultiBranchProject mbp = mockMbp(organizationFolder, aliceCredentialId);

        try {
            //Bob trying to access content but his credential is not setup so should fail
            new GithubScmContentProvider().getContent(staplerRequest, mbp);
        }catch (ServiceException.PreconditionRequired e){
            assertEquals("Can't access content from github: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }

    @Test
    public void unauthorizedAccessToContentForMbpGHEShouldFail() throws UnirestException, IOException {
        User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createGithubEnterpriseCredential(alice);

        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        OrganizationFolder organizationFolder = mockOrgFolder(alice, aliceCredentialId);

        MultiBranchProject mbp = mockMbp(organizationFolder, aliceCredentialId);

        try {
            //Bob trying to access content but his credential is not setup so should fail
            new GithubScmContentProvider().getContent(staplerRequest, mbp);
        }catch (ServiceException.PreconditionRequired e){
            assertEquals("Can't access content from github: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }

    @Test
    public void saveContentToOrgFolder() throws UnirestException, IOException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, credentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        GithubFile file = (GithubFile) new GithubScmContentProvider().saveContent(staplerRequest, orgFolder);
        assertEquals("Jenkinsfile", file.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", file.getContent().getSha());
        assertEquals("PR-demo", file.getContent().getRepo());
        assertEquals("cloudbeers", file.getContent().getOwner());
    }

    @Test
    public void saveContentToOrgFolderGHE() throws UnirestException, IOException {
        String credentialId = createGithubEnterpriseCredential();

        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, credentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        GithubFile file = (GithubFile) new GithubScmContentProvider().saveContent(staplerRequest, orgFolder);
        assertEquals("Jenkinsfile", file.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", file.getContent().getSha());
        assertEquals("PR-demo", file.getContent().getRepo());
        assertEquals("cloudbeers", file.getContent().getOwner());
    }


    @Test
    public void unauthorizedSaveContentToOrgFolderShouldFail() throws UnirestException, IOException {
        User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createGithubCredential(alice);


        StaplerRequest staplerRequest = mockStapler();

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, aliceCredentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));
        try {
            //Bob trying to access content but his credential is not setup so should fail
            new GithubScmContentProvider().saveContent(staplerRequest, orgFolder);
        }catch (ServiceException.PreconditionRequired e){
            assertEquals("Can't access content from github: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }

    @Test
    public void unauthorizedSaveContentToOrgFolderGHEShouldFail() throws UnirestException, IOException {
        User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createGithubEnterpriseCredential(alice);


        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, aliceCredentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));
        try {
            //Bob trying to access content but his credential is not setup so should fail
            new GithubScmContentProvider().saveContent(staplerRequest, orgFolder);
        }catch (ServiceException.PreconditionRequired e){
            assertEquals("Can't access content from github: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }


    @Test
    public void saveContentToMbp() throws UnirestException, IOException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, credentialId);

        MultiBranchProject mbp = mockMbp(orgFolder, credentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        GithubFile file = (GithubFile) new GithubScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", file.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", file.getContent().getSha());
        assertEquals("PR-demo", file.getContent().getRepo());
        assertEquals("cloudbeers", file.getContent().getOwner());
    }

    @Test
    public void saveContentToMbpGHE() throws UnirestException, IOException {
        String credentialId = createGithubEnterpriseCredential();

        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, credentialId);

        MultiBranchProject mbp = mockMbp(orgFolder, credentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        GithubFile file = (GithubFile) new GithubScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", file.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", file.getContent().getSha());
        assertEquals("PR-demo", file.getContent().getRepo());
        assertEquals("cloudbeers", file.getContent().getOwner());
    }

    @Test
    public void unauthorizedSaveContentToMbpShouldFail() throws UnirestException, IOException {
        User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createGithubCredential(alice);

        StaplerRequest staplerRequest = mockStapler();

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, aliceCredentialId);

        MultiBranchProject mbp = mockMbp(orgFolder, aliceCredentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        try {
            //Bob trying to access content but his credential is not setup so should fail
            new GithubScmContentProvider().saveContent(staplerRequest, mbp);
        }catch (ServiceException.PreconditionRequired e){
            assertEquals("Can't access content from github: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }

    @Test
    public void unauthorizedSaveContentToMbpGHEShouldFail() throws UnirestException, IOException {
        User alice = j.jenkins.getUser("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createGithubEnterpriseCredential(alice);

        StaplerRequest staplerRequest = mockStapler(GithubEnterpriseScm.ID);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test1").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, aliceCredentialId);

        MultiBranchProject mbp = mockMbp(orgFolder, aliceCredentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test1\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        try {
            //Bob trying to access content but his credential is not setup so should fail
            new GithubScmContentProvider().saveContent(staplerRequest, mbp);
        }catch (ServiceException.PreconditionRequired e){
            assertEquals("Can't access content from github: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }

    @Test
    public void saveContentToMbpMissingBranch() throws UnirestException, IOException {
        String credentialId = createGithubCredential();

        StaplerRequest staplerRequest = mockStapler();

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n")
                .branch("test2").message("another commit").owner("cloudbeers").path("Jankinsfile").repo("PR-demo").sha("e23b8ef5c2c4244889bf94db6c05cc08ea138aef").build();

        when(staplerRequest.bindJSON(Mockito.eq(GithubScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new GithubScmSaveFileRequest(content));

        OrganizationFolder orgFolder = mockOrgFolder(user, credentialId);

        MultiBranchProject mbp = mockMbp(orgFolder, credentialId);

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"test2\",\n" +
                "    \"repo\" : \"PR-demo\",\n" +
                "    \"sha\" : \"e23b8ef5c2c4244889bf94db6c05cc08ea138aef\",\n" +
                "    \"base64Data\" : "+"\"c2xlZXAgMTUKbm9kZSB7CiAgY2hlY2tvdXQgc2NtCiAgc2ggJ2xzIC1sJwp9\\nCnNsZWVwIDE1Cg==\\n\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        GithubFile file = (GithubFile) new GithubScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", file.getContent().getName());
        assertEquals("e23b8ef5c2c4244889bf94db6c05cc08ea138aef", file.getContent().getSha());
        assertEquals("PR-demo", file.getContent().getRepo());
        assertEquals("cloudbeers", file.getContent().getOwner());
    }

    @Test
    public void testScmProperties() throws Exception {
        // ensure the cloud provider works with cloud org folder
        String credentialId = createGithubCredential();
        OrganizationFolder orgFolder = mockOrgFolder(credentialId);
        // unfortunately overriding the GitHub apiUrl for WireMock returns a "localhost" URL here, so we mock the call
        when(((GitHubSCMNavigator) orgFolder.getSCMNavigators().get(0)).getApiUri()).thenReturn(GitHubSCMSource.GITHUB_URL);
        ScmContentProvider provider = new GithubScmContentProvider();
        assertTrue(provider.support(orgFolder));
        assertEquals(provider.getScmId(), GithubScm.ID);
        assertEquals(provider.getApiUrl(orgFolder), GitHubSCMSource.GITHUB_URL);
        // ensure the cloud provider doesn't support enterprise org folder
        orgFolder = mockOrgFolder(createGithubEnterpriseCredential());
        assertFalse(provider.support(orgFolder));
    }

    protected StaplerRequest mockStapler(){
        return mockStapler("github");
    }
    private StaplerRequest mockStapler(String scmId){
        mockStatic(Stapler.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        when(Stapler.getCurrentRequest()).thenReturn(staplerRequest);
        when(staplerRequest.getRequestURI()).thenReturn("http://localhost:8080/jenkins/blue/rest/");
        when(staplerRequest.getParameter("path")).thenReturn("Jenkinsfile");
        when(staplerRequest.getParameter("repo")).thenReturn("PR-demo");

        // GithubScmContentProvider determines SCM using apiUrl but with wiremock
        // apiUrl is localhost and not github so we use this parameter from test only to tell scm id
        when(staplerRequest.getParameter("scmId")).thenReturn(scmId);
        when(staplerRequest.getParameter("apiUrl")).thenReturn(githubApiUrl);
        return staplerRequest;
    }

    private OrganizationFolder mockOrgFolder(User user, String credentialId){

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
        when(orgFolder.getParent()).thenReturn(j.jenkins);
        Domain domain = mock(Domain.class);
        when(domain.getName()).thenReturn(GithubScm.DOMAIN_NAME);
        when(folderProperty.getDomain()).thenReturn(domain);
        return orgFolder;
    }
}

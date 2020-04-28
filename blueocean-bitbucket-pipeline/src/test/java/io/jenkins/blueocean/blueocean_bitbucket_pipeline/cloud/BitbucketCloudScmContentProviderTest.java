package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.model.User;
import hudson.tasks.Mailer;
import hudson.util.DescribableList;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketScmSaveFileRequest;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.BitbucketServerScmContentProvider;
import io.jenkins.blueocean.commons.ServiceException;
import io.jenkins.blueocean.rest.impl.pipeline.ScmContentProvider;
import io.jenkins.blueocean.rest.impl.pipeline.credential.BlueOceanCredentialsProvider;
import io.jenkins.blueocean.rest.impl.pipeline.scm.GitContent;
import io.jenkins.blueocean.rest.impl.pipeline.scm.ScmFile;
import jenkins.branch.MultiBranchProject;
import jenkins.scm.api.SCMSource;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Stapler.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class BitbucketCloudScmContentProviderTest extends BbCloudWireMock {
    @Test
    public void getContent() throws UnirestException, IOException {
        String credentialId = createCredential(BitbucketCloudScm.ID);
        StaplerRequest staplerRequest = mockStapler();
        MultiBranchProject mbp = mockMbp(credentialId);

        ScmFile<GitContent> content = (ScmFile<GitContent>) new BitbucketCloudScmContentProvider().getContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("04553981a05754d4bffef56a59d9d996d500301c", content.getContent().getCommitId());
        assertEquals("demo1", content.getContent().getRepo());
        assertEquals(BbCloudWireMock.USER_UUID, content.getContent().getOwner());
    }

    @Test
    public void unauthorizedAccessToContentShouldFail() throws UnirestException, IOException {
        User alice = User.get("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createCredential(BitbucketCloudScm.ID, "cloud", alice);

        StaplerRequest staplerRequest = mockStapler();

        MultiBranchProject mbp = mockMbp(aliceCredentialId, alice);

        try {
            //Bob trying to access content but his credential is not setup so should fail
            new BitbucketCloudScmContentProvider().getContent(staplerRequest, mbp);
        } catch (ServiceException.PreconditionRequired e) {
            assertEquals("Can't access content from Bitbucket: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }


    @Test
    public void newContent() throws UnirestException, IOException {
        String credentialId = createCredential(BitbucketCloudScm.ID);
        StaplerRequest staplerRequest = mockStapler();
        MultiBranchProject mbp = mockMbp(credentialId);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("VGhpcyBpcyB0ZXN0IGNvbnRlbnQgaW4gbmV3IGZpbGUK")
                .branch("master").message("new commit").owner(BbCloudWireMock.USER_UUID).path("foo").repo("demo1").build();

        when(staplerRequest.bindJSON(Mockito.eq(BitbucketScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new BitbucketScmSaveFileRequest(content));

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"foo\",\n" +
                "    \"branch\" : \"master\",\n" +
                "    \"repo\" : \"demo1\",\n" +
                "    \"base64Data\" : " + "\"VGhpcyBpcyB0ZXN0IGNvbnRlbnQgaW4gbmV3IGZpbGUK\"" +
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        ScmFile<GitContent> respContent = (ScmFile<GitContent>) new BitbucketCloudScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("foo", respContent.getContent().getName());
        assertEquals(respContent.getContent().getCommitId(), respContent.getContent().getCommitId());
        assertEquals("demo1", respContent.getContent().getRepo());
        assertEquals(BbCloudWireMock.USER_UUID, respContent.getContent().getOwner());
        assertEquals("master", respContent.getContent().getBranch());
    }

    @Test
    public void checkScmProperties() throws Exception {
        // ensure cloud provider works with cloud multibranch pipeline
        String credentialId = createCredential(BitbucketCloudScm.ID, authenticatedUser);
        MultiBranchProject mbp = mockMbp(credentialId);
        ScmContentProvider provider = new BitbucketCloudScmContentProvider();
        // unfortunately overriding the apiUrl for WireMock returns a "localhost" URL here, so we mock the call
        when(((BitbucketSCMSource) mbp.getSCMSources().get(0)).getServerUrl()).thenReturn(BitbucketCloudScm.API_URL);
        assertTrue(provider.support(mbp));
        assertEquals(provider.getScmId(), BitbucketCloudScm.ID);
        assertEquals(provider.getApiUrl(mbp), BitbucketCloudScm.API_URL);
        // ensure server provider doesn't work with cloud multibranch pipeline
        provider = new BitbucketServerScmContentProvider();
        assertFalse(provider.support(mbp));
    }


    @Test
    public void unauthorizedSaveContentShouldFail() throws UnirestException, IOException {
        User alice = User.get("alice");
        alice.setFullName("Alice Cooper");
        alice.addProperty(new Mailer.UserProperty("alice@jenkins-ci.org"));

        String aliceCredentialId = createCredential(BitbucketCloudScm.ID, alice);
        StaplerRequest staplerRequest = mockStapler();
        MultiBranchProject mbp = mockMbp(aliceCredentialId, alice);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("bm9kZXsKICBlY2hvICdoZWxsbyB3b3JsZCEnCn0K")
                .branch("master").message("new commit").owner("TESTP").path("README.md").repo("pipeline-demo-test").build();

        when(staplerRequest.bindJSON(Mockito.eq(BitbucketScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new BitbucketScmSaveFileRequest(content));

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"new commit\",\n" +
                "    \"path\" : \"README.md\",\n" +
                "    \"branch\" : \"master\",\n" +
                "    \"repo\" : \"pipeline-demo-test\",\n" +
                "    \"base64Data\" : " + "\"bm9kZXsKICBlY2hvICdoZWxsbyB3b3JsZCEnCn0K\"" +
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        try {
            new BitbucketCloudScmContentProvider().saveContent(staplerRequest, mbp);
        } catch (ServiceException.PreconditionRequired e) {
            assertEquals("Can't access content from Bitbucket: no credential found", e.getMessage());
            return;
        }
        fail("Should have failed with PreConditionException");
    }

    private StaplerRequest mockStapler() {
        mockStatic(Stapler.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        when(Stapler.getCurrentRequest()).thenReturn(staplerRequest);
        when(staplerRequest.getRequestURI()).thenReturn("http://localhost:8080/jenkins/blue/rest/");
        when(staplerRequest.getParameter("path")).thenReturn("Jenkinsfile");
        when(staplerRequest.getParameter("repo")).thenReturn("demo1");
        when(staplerRequest.getParameter("scmId")).thenReturn(BitbucketCloudScm.ID);
        return staplerRequest;
    }

    private MultiBranchProject mockMbp(String credentialId) {
        return mockMbp(credentialId, authenticatedUser);
    }

    private MultiBranchProject mockMbp(String credentialId, User user) {
        MultiBranchProject mbp = mock(MultiBranchProject.class);
        when(mbp.getName()).thenReturn("pipeline1");
        when(mbp.getParent()).thenReturn(j.jenkins);
        BitbucketSCMSource scmSource = mock(BitbucketSCMSource.class);
        when(scmSource.getServerUrl()).thenReturn(apiUrl);
        when(scmSource.getCredentialsId()).thenReturn(credentialId);
        when(scmSource.getRepoOwner()).thenReturn(USER_UUID);
        when(scmSource.getRepository()).thenReturn("demo1");
        when(mbp.getSCMSources()).thenReturn(Lists.<SCMSource>newArrayList(scmSource));

        //mock blueocean credential provider stuff
        BlueOceanCredentialsProvider.FolderPropertyImpl folderProperty = mock(BlueOceanCredentialsProvider.FolderPropertyImpl.class);
        DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor> properties = new DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor>(mbp);
        properties.add(new BlueOceanCredentialsProvider.FolderPropertyImpl(
                user.getId(), credentialId,
                BlueOceanCredentialsProvider.createDomain(apiUrl)
        ));
        Domain domain = mock(Domain.class);
        when(domain.getName()).thenReturn(BitbucketCloudScm.DOMAIN_NAME);
        when(folderProperty.getDomain()).thenReturn(domain);

        when(mbp.getProperties()).thenReturn(properties);
        return mbp;
    }

}

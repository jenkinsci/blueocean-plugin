package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.util.DescribableList;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketScmContentProvider;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketScmSaveFileRequest;
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

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Stapler.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*"})
public class BitbucketContentProviderTest extends BbServerWireMock {

    @Test
    public void getContent() throws UnirestException, IOException {
        String credentialId = createCredential(BitbucketServerScm.ID);
        StaplerRequest staplerRequest = mockStapler();
        MultiBranchProject mbp = mockMbp(credentialId);

        ScmFile<GitContent> content = (ScmFile<GitContent>) new BitbucketScmContentProvider().getContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("0bae0ddbed2e897d3b44abc3aca9ba26e2f61710", content.getContent().getCommitId());
        assertEquals("pipeline-demo-test", content.getContent().getRepo());
        assertEquals("TESTP", content.getContent().getOwner());
    }

    @Test
    public void newContent() throws UnirestException, IOException {
        String credentialId = createCredential(BitbucketServerScm.ID);
        StaplerRequest staplerRequest = mockStapler();
        MultiBranchProject mbp = mockMbp(credentialId);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("bm9kZXsKICBlY2hvICdoZWxsbyB3b3JsZCEnCn0K")
                .branch("master").message("new commit").owner("TESTP").path("README.md").repo("pipeline-demo-test").build();

        when(staplerRequest.bindJSON(Mockito.eq(BitbucketScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new BitbucketScmSaveFileRequest(content));

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"new commit\",\n" +
                "    \"path\" : \"README.md\",\n" +
                "    \"branch\" : \"master\",\n" +
                "    \"repo\" : \"pipeline-demo-test\",\n" +
                "    \"base64Data\" : "+"\"bm9kZXsKICBlY2hvICdoZWxsbyB3b3JsZCEnCn0K\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        ScmFile<GitContent> respContent = (ScmFile<GitContent>) new BitbucketScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("README.md", respContent.getContent().getName());
        assertEquals("a77840d4108db2befe6c616723eb3f4485af5d24", respContent.getContent().getCommitId());
        assertEquals("pipeline-demo-test", respContent.getContent().getRepo());
        assertEquals("TESTP", respContent.getContent().getOwner());
        assertEquals("master", respContent.getContent().getBranch());
    }

    @Test
    public void updateContent() throws UnirestException, IOException {
        String credentialId = createCredential(BitbucketServerScm.ID);
        StaplerRequest staplerRequest = mockStapler();
        MultiBranchProject mbp = mockMbp(credentialId);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("bm9kZXsKICBlY2hvICdoZWxsbyB3b3JsZCEnCn0K")
                .branch("master").message("another commit").owner("TESTP").path("Jenkinsfile").repo("pipeline-demo-test").commitId("0bae0ddbed2e897d3b44abc3aca9ba26e2f61710").build();

        when(staplerRequest.bindJSON(Mockito.eq(BitbucketScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new BitbucketScmSaveFileRequest(content));

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"another commit\",\n" +
                "    \"path\" : \"Jenkinsfile\",\n" +
                "    \"branch\" : \"master\",\n" +
                "    \"repo\" : \"pipeline-demo-test\",\n" +
                "    \"commitId\" : \"0bae0ddbed2e897d3b44abc3aca9ba26e2f61710\",\n" +
                "    \"base64Data\" : "+"\"bm9kZXsKICBlY2hvICdoZWxsbyB3b3JsZCEnCn0K\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        ScmFile<GitContent> respContent = (ScmFile<GitContent>) new BitbucketScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", respContent.getContent().getName());
        assertEquals("e587b620844b1b230783976f00cfb8383488aeca", respContent.getContent().getCommitId());
        assertEquals("pipeline-demo-test", respContent.getContent().getRepo());
        assertEquals("TESTP", respContent.getContent().getOwner());
        assertEquals("master", respContent.getContent().getBranch());
    }

    private StaplerRequest mockStapler(){
        mockStatic(Stapler.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        when(Stapler.getCurrentRequest()).thenReturn(staplerRequest);
        when(staplerRequest.getRequestURI()).thenReturn("http://localhost:8080/jenkins/blue/rest/");
        when(staplerRequest.getParameter("path")).thenReturn("Jenkinsfile");
        when(staplerRequest.getParameter("repo")).thenReturn("pipeline-demo-test");
        return staplerRequest;
    }

    private MultiBranchProject mockMbp(String credentialId){
        MultiBranchProject mbp = mock(MultiBranchProject.class);
        when(mbp.getName()).thenReturn("pipeline1");
        when(mbp.getParent()).thenReturn(j.jenkins);
        BitbucketSCMSource scmSource = mock(BitbucketSCMSource.class);
        when(scmSource.getServerUrl()).thenReturn(apiUrl);
        when(scmSource.getCredentialsId()).thenReturn(credentialId);
        when(scmSource.getRepoOwner()).thenReturn("TESTP");
        when(scmSource.getRepository()).thenReturn("pipeline-demo-test");
        when(mbp.getSCMSources()).thenReturn(Lists.<SCMSource>newArrayList(scmSource));

        //mock blueocean credential provider stuff
        BlueOceanCredentialsProvider.FolderPropertyImpl folderProperty = mock(BlueOceanCredentialsProvider.FolderPropertyImpl.class);
        DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> properties = new DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor>(mbp);
        properties.add(new BlueOceanCredentialsProvider.FolderPropertyImpl(
                authenticatedUser.getId(), credentialId,
                BlueOceanCredentialsProvider.createDomain(apiUrl)
        ));
        Domain domain = mock(Domain.class);
        when(domain.getName()).thenReturn(BitbucketServerScm.DOMAIN_NAME);
        when(folderProperty.getDomain()).thenReturn(domain);

        when(mbp.getProperties()).thenReturn(properties);
        return mbp;
    }
}

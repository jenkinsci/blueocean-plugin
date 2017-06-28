package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.util.DescribableList;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
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
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*","javax.net.SocketFactory"})
public class BbCloudContentProviderTest extends BbCloudWireMock{
    @Test
    public void getContent() throws UnirestException, IOException {
        String credentialId = createCredential(BitbucketCloudScm.ID, "cloud");
        StaplerRequest staplerRequest = mockStapler();
        MultiBranchProject mbp = mockMbp(credentialId);

        ScmFile<GitContent> content = (ScmFile<GitContent>) new BitbucketScmContentProvider().getContent(staplerRequest, mbp);
        assertEquals("Jenkinsfile", content.getContent().getName());
        assertEquals("04553981a05754d4bffef56a59d9d996d500301c", content.getContent().getCommitId());
        assertEquals("demo1", content.getContent().getRepo());
        assertEquals("vivekp7", content.getContent().getOwner());
    }

    @Test
    public void newContent() throws UnirestException, IOException {
        String credentialId = createCredential(BitbucketCloudScm.ID, "cloud");
        StaplerRequest staplerRequest = mockStapler();
        MultiBranchProject mbp = mockMbp(credentialId);

        GitContent content = new GitContent.Builder().autoCreateBranch(true).base64Data("VGhpcyBpcyB0ZXN0IGNvbnRlbnQgaW4gbmV3IGZpbGUK")
                .branch("master").message("new commit").owner("vivekp7").path("foo").repo("demo1").build();

        when(staplerRequest.bindJSON(Mockito.eq(BitbucketScmSaveFileRequest.class), Mockito.any(JSONObject.class))).thenReturn(new BitbucketScmSaveFileRequest(content));

        String request = "{\n" +
                "  \"content\" : {\n" +
                "    \"message\" : \"first commit\",\n" +
                "    \"path\" : \"foo\",\n" +
                "    \"branch\" : \"master\",\n" +
                "    \"repo\" : \"demo1\",\n" +
                "    \"base64Data\" : "+"\"VGhpcyBpcyB0ZXN0IGNvbnRlbnQgaW4gbmV3IGZpbGUK\""+
                "  }\n" +
                "}";

        when(staplerRequest.getReader()).thenReturn(new BufferedReader(new StringReader(request), request.length()));

        ScmFile<GitContent> respContent = (ScmFile<GitContent>) new BitbucketScmContentProvider().saveContent(staplerRequest, mbp);
        assertEquals("foo", respContent.getContent().getName());
        assertEquals(respContent.getContent().getCommitId(), respContent.getContent().getCommitId());
        assertEquals("demo1", respContent.getContent().getRepo());
        assertEquals("vivekp7", respContent.getContent().getOwner());
        assertEquals("master", respContent.getContent().getBranch());
    }

    private StaplerRequest mockStapler(){
        mockStatic(Stapler.class);
        StaplerRequest staplerRequest = mock(StaplerRequest.class);
        when(Stapler.getCurrentRequest()).thenReturn(staplerRequest);
        when(staplerRequest.getRequestURI()).thenReturn("http://localhost:8080/jenkins/blue/rest/");
        when(staplerRequest.getParameter("path")).thenReturn("Jenkinsfile");
        when(staplerRequest.getParameter("repo")).thenReturn("demo1");
        when(staplerRequest.getHeader(BitbucketApi.X_BB_API_TEST_MODE_HEADER)).thenReturn("cloud");
        return staplerRequest;
    }

    private MultiBranchProject mockMbp(String credentialId){
        MultiBranchProject mbp = mock(MultiBranchProject.class);
        when(mbp.getName()).thenReturn("pipeline1");
        when(mbp.getParent()).thenReturn(j.jenkins);
        BitbucketSCMSource scmSource = mock(BitbucketSCMSource.class);
        when(scmSource.getBitbucketServerUrl()).thenReturn(apiUrl);
        when(scmSource.getCredentialsId()).thenReturn(credentialId);
        when(scmSource.getRepoOwner()).thenReturn("vivekp7");
        when(scmSource.getRepository()).thenReturn("demo1");
        when(mbp.getSCMSources()).thenReturn(Lists.<SCMSource>newArrayList(scmSource));

        //mock blueocean credential provider stuff
        BlueOceanCredentialsProvider.FolderPropertyImpl folderProperty = mock(BlueOceanCredentialsProvider.FolderPropertyImpl.class);
        DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor> properties = new DescribableList<AbstractFolderProperty<?>,AbstractFolderPropertyDescriptor>(mbp);
        properties.add(new BlueOceanCredentialsProvider.FolderPropertyImpl(
                authenticatedUser.getId(), credentialId,
                BlueOceanCredentialsProvider.createDomain(apiUrl)
        ));
        Domain domain = mock(Domain.class);
        when(domain.getName()).thenReturn(BitbucketCloudScm.DOMAIN_NAME);
        when(folderProperty.getDomain()).thenReturn(domain);

        when(mbp.getProperties()).thenReturn(properties);
        return mbp;
    }

}

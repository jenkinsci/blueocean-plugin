package io.jenkins.blueocean.blueocean_bitbucket_pipeline.server;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.HttpRequest;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.HttpResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.server.model.BbServerBranch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Secret.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class BitbucketApiTest extends BbServerWireMock {
    private BitbucketApi api;

    @Override
    public void setup() throws Exception {
        super.setup();
        this.api = new BitbucketServerApi(apiUrl, getMockedCredentials());
    }

    @Test
    public void getUser(){
        BbUser user = api.getUser();
        assertNotNull(user);
        assertEquals(getUserName(), user.getSlug());
    }

    @Test
    public void getProjects() throws JsonProcessingException {
        BbPage<BbOrg> projects = api.getOrgs(1, 100);
        assertEquals(3, projects.getSize());
        assertEquals("~vivek", projects.getValues().get(0).getKey());
        assertEquals("TEST", projects.getValues().get(1).getKey());
        assertEquals("TESTP", projects.getValues().get(2).getKey());

    }

    @Test
    public void getProject() throws JsonProcessingException {
        BbOrg project = api.getOrg("TESTP");
        assertEquals("TESTP", project.getKey());
        assertEquals("testproject1", project.getName());
    }

    @Test
    public void getRepos(){
        BbPage<BbRepo> repos = api.getRepos("TESTP", 1, 100);
        assertEquals(2, repos.getSize());
        assertEquals("empty-repo-test", repos.getValues().get(0).getSlug());
        assertEquals("pipeline-demo-test", repos.getValues().get(1).getSlug());
    }

    @Test
    public void getPersonalRepos(){
        BbPage<BbRepo> repos = api.getRepos("~vivek", 1, 100);
        assertEquals(1, repos.getSize());
        assertEquals("personalrepo1", repos.getValues().get(0).getSlug());
    }

    @Test
    public void getRepo(){
        BbRepo repo = api.getRepo("TESTP", "pipeline-demo-test");
        assertEquals("pipeline-demo-test", repo.getSlug());
    }

    @Test
    public void getRepoContent() throws JsonProcessingException, UnsupportedEncodingException {
        BbBranch branch = api.getDefaultBranch("TESTP","pipeline-demo-test");
        assertNotNull(branch);
        String content = api.getContent("TESTP", "pipeline-demo-test", "Jenkinsfile", branch.getLatestCommit());
        assertEquals("node{\n" +
                "  echo 'hello world'\n" +
                "}", content);
    }

    @Test
    public void updateRepoContent() throws JsonProcessingException, UnsupportedEncodingException {
        BbBranch branch = api.getBranch("TESTP", "pipeline-demo-test", "master");
        assertEquals("master", branch.getDisplayId());
        assertTrue(branch instanceof BbServerBranch);
        assertEquals("refs/heads/master", ((BbServerBranch)branch).getId());
        assertNotNull(branch.getLatestCommit());

        String content = api.getContent("TESTP", "pipeline-demo-test", "Jenkinsfile", branch.getLatestCommit());
        assertEquals("node{\n" +
                "  echo 'hello world'\n" +
                "}", content);

        //update content
        api.saveContent("TESTP", "pipeline-demo-test","Jenkinsfile","node{\n" +
                "  echo 'hello world!'\n" +
                "}", "another commit", "master",null,branch.getLatestCommit());
    }

    @Test
    public void createNewRepoContent() throws JsonProcessingException, UnsupportedEncodingException {
        boolean exists = api.fileExists("TESTP", "pipeline-demo-test", "README.md", "master");
        assertFalse(exists);

        //create new file
        BbSaveContentResponse saveResponse = api.saveContent("TESTP","pipeline-demo-test","README.md",
                "This is test content in new file",
                "another commit", "master",null,null);
        assertNotNull(saveResponse.getCommitId());
        String content = api.getContent("TESTP", "pipeline-demo-test", "README.md", (String) saveResponse.getCommitId());
        assertEquals("This is test content in new file", content);
    }

    @Test
    public void defaultBranch(){
        BbBranch branch = api.getDefaultBranch("TESTP","pipeline-demo-test");
        assertNotNull(branch);
        assertEquals("master", branch.getDisplayId());
        assertTrue(branch instanceof BbServerBranch);
        assertEquals("refs/heads/master", ((BbServerBranch)branch).getId());
    }

    @Test
    public void testAutoRedirectDisabled() {
        HttpResponse response = new HttpRequest.HttpRequestBuilder(apiUrl).build().get(apiUrl+"/rest/api/1.0/test-redirect");
        assertEquals(302, response.getStatus());
        assertEquals("http://localhost:7990/bitbucket/rest/api/1.0/redirect-test-success", response.getHeader("Location"));
    }

    @Test
    public void testEmptyRepo(){
        boolean empty = api.isEmptyRepo("TESTP", "empty-repo-test");
        assertTrue(empty);
        //create new file
        BbSaveContentResponse saveResponse = api.saveContent("TESTP","empty-repo-test","README.md",
                "This is test content in new file",
                "another commit", "master",null,null);
        assertNotNull(saveResponse.getCommitId());
        String content = api.getContent("TESTP", "empty-repo-test", "README.md", (String) saveResponse.getCommitId());
        assertEquals("This is test content in new file", content);
    }

    @Test
    public void testEmptyRepo204(){
        BbBranch branch = api.getDefaultBranch("TESTP","empty1");
        assertNull(branch);
    }

    @Test
    public void testDefaultBranchPre5_6_0(){
        BbBranch branch = api.getDefaultBranch("TESTP","empty-repo-test");
        assertNull(branch);
    }

    @Test
    public void testDefaultBranch5_6_0(){
        BbBranch branch = api.getDefaultBranch("TESTP","empty1");
        assertNull(branch);
    }

    @Test
    public void testCreateNewBranchOnExistingRepo(){
        BbBranch branch = api.getDefaultBranch("TESTP","pipeline-demo-test");
        BbBranch newBranch = api.createBranch("TESTP", "pipeline-demo-test",
                ImmutableMap.of("name", "feature1",
                        "startPoint", branch.getLatestCommit(),
                        "message", "new branch"));
        assertEquals("feature1", newBranch.getDisplayId());
        assertEquals(branch.getLatestCommit(), newBranch.getLatestCommit());
    }

    private StandardUsernamePasswordCredentials getMockedCredentials(){
        final Secret secret = Mockito.mock(Secret.class);
        when(secret.getPlainText()).thenReturn(getPassword());

        PowerMockito.mockStatic(Secret.class);
        when(Secret.toString(secret)).thenReturn(getPassword());

        return new StandardUsernamePasswordCredentials(){
            @Override
            public CredentialsScope getScope() {
                return CredentialsScope.SYSTEM;
            }
            @NonNull
            @Override
            public CredentialsDescriptor getDescriptor() {
                return new CredentialsDescriptor(){

                };
            }
            @NonNull
            @Override
            public String getUsername() {
                return getUserName();
            }

            @NonNull
            @Override
            public String getId() {
                return "bitbucket-api-test";
            }

            @NonNull
            @Override
            public String getDescription() {
                return "";
            }

            @NonNull
            @Override
            public Secret getPassword() {
                return secret;
            }
        };
    }

}

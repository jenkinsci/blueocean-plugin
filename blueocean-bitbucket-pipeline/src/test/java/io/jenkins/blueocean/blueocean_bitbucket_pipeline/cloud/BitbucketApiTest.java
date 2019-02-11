package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.core.JsonProcessingException;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.Secret;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketApi;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbBranch;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbOrg;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbPage;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbRepo;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbSaveContentResponse;
import io.jenkins.blueocean.blueocean_bitbucket_pipeline.model.BbUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Secret.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class BitbucketApiTest  extends BbCloudWireMock{

    private BitbucketApi api;

    @Override
    public void setup() throws Exception {
        super.setup();
        this.api = new BitbucketCloudApi(apiUrl, getMockedCredentials());
    }

    @Test
    public void getUser(){
        BbUser user = api.getUser();
        assertNotNull(user);
        assertEquals(getUserName(), user.getSlug());
        assertEquals("https://bitbucket.org/account/vivekp7/avatar/50/", user.getAvatar());
    }

    @Test
    public void getTeams() throws JsonProcessingException {
        BbPage<BbOrg> teams = api.getOrgs(1, 100);
        assertEquals(2, teams.getSize());
        assertEquals("vivekp7", teams.getValues().get(0).getKey());
        assertEquals("Vivek Pandey", teams.getValues().get(0).getName());
        assertEquals("https://bitbucket.org/account/vivekp7/avatar/50/", teams.getValues().get(0).getAvatar());

        assertEquals("vivektestteam", teams.getValues().get(1).getKey());
        assertEquals("Vivek's Team", teams.getValues().get(1).getName());
        assertEquals("https://bitbucket.org/account/vivektestteam/avatar/50/", teams.getValues().get(1).getAvatar());
    }

    @Test
    public void getUserTeam() throws JsonProcessingException {
        BbOrg team = api.getOrg("vivekp7");
        assertEquals("vivekp7", team.getKey());
        assertEquals("Vivek Pandey", team.getName());
        assertEquals("https://bitbucket.org/account/vivekp7/avatar/50/", team.getAvatar());
    }

    @Test
    public void getUserTeamUsingEmail() throws JsonProcessingException {
        api = new BitbucketCloudApi(apiUrl, getMockedCredentials("x.y@gmail.com"));
        BbOrg team = api.getOrg("vivekp7");
        assertEquals("vivekp7", team.getKey());
        assertEquals("Vivek Pandey", team.getName());
        assertEquals("https://bitbucket.org/account/vivekp7/avatar/50/", team.getAvatar());
    }


    @Test
    public void getTeam() throws JsonProcessingException {
        BbOrg team = api.getOrg("vivektestteam");
        assertEquals("vivektestteam", team.getKey());
        assertEquals("Vivek's Team", team.getName());
        assertEquals("https://bitbucket.org/account/vivektestteam/avatar/50/", team.getAvatar());
    }

    @Test
    public void getRepos(){
        BbPage<BbRepo> repos = api.getRepos("vivektestteam", 1, 100);
        assertEquals(2, repos.getSize());
        assertEquals("pipeline-demo-test", repos.getValues().get(0).getSlug());
        assertEquals("emptyrepo", repos.getValues().get(1).getSlug());
    }

    @Test
    public void getRepo(){
        BbRepo repo = api.getRepo("vivektestteam", "pipeline-demo-test");
        assertEquals("pipeline-demo-test", repo.getSlug());
    }

    @Test
    public void getRepoContent() throws JsonProcessingException, UnsupportedEncodingException {
        BbBranch branch = api.getDefaultBranch("vivekp7","demo1");
        assertNotNull(branch);
        String content = api.getContent("vivekp7", "demo1", "Jenkinsfile", branch.getLatestCommit());
        assertEquals("node{\n" +
                "  echo 'hello world!'\n" +
                "}", content);
    }


    @Test
    public void createNewRepoContent() throws JsonProcessingException, UnsupportedEncodingException {
        //create new file
        BbSaveContentResponse saveResponse = api.saveContent("vivekp7","demo1","foo",
                "This is test content in new file",
                "first commit", "null",null,null);
        assertNotNull(saveResponse.getCommitId());
        String content = api.getContent("vivekp7", "demo1", "foo", (String) saveResponse.getCommitId());
        assertEquals("This is test content in new file", content);
    }

    //This is duplicated code from server, we can't move to base class as inheritance won't work with @RunWith annotation
    //from subclasses
    private StandardUsernamePasswordCredentials getMockedCredentials(){
        return getMockedCredentials(getUserName());
    }

    private StandardUsernamePasswordCredentials getMockedCredentials(final String username){
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
                return username;
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

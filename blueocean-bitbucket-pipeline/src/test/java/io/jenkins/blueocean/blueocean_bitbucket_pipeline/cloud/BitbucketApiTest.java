package io.jenkins.blueocean.blueocean_bitbucket_pipeline.cloud;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Vivek Pandey
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Secret.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class BitbucketApiTest extends BbCloudWireMock {

    private BitbucketApi api;
    private static String USER_KEY = "{1c5c9255-d59f-47e2-b5c7-52269c0332b9}"; // vivekp7;
    private static String TEAM_KEY = "{47cd7cf2-ca31-4c90-bc0e-4c7ef67f9dfe}"; // vivektestteam

    @Override
    public void setup() throws Exception {
        super.setup();
        this.api = new BitbucketCloudApi(apiUrl, getMockedCredentials());
    }

    @Test
    public void getUser() {
        BbUser user = api.getUser();
        assertNotNull(user);
        assertEquals(USER_KEY, user.getSlug());
        assertEquals("https://bitbucket.org/account/" + USER_KEY + "/avatar/50/", user.getAvatar());
    }

    @Test
    public void getTeams() {
        BbPage<BbOrg> teams = api.getOrgs(1, 100);
        assertEquals(2, teams.getSize());
        assertEquals(USER_KEY, teams.getValues().get(0).getKey());
        assertEquals("Vivek Pandey", teams.getValues().get(0).getName());
        assertEquals("https://bitbucket.org/account/" + USER_KEY + "/avatar/50/", teams.getValues().get(0).getAvatar());

        assertEquals(TEAM_KEY, teams.getValues().get(1).getKey());
        assertEquals("Vivek's Team", teams.getValues().get(1).getName());
        assertEquals("https://bitbucket.org/account/" + TEAM_KEY + "/avatar/50/", teams.getValues().get(1).getAvatar());
    }

    @Test
    public void getUserTeam() {
        BbOrg team = api.getOrg(USER_KEY);
        assertEquals(USER_KEY, team.getKey());
        assertEquals("Vivek Pandey", team.getName());
        assertEquals("https://bitbucket.org/account/" + USER_KEY + "/avatar/50/", team.getAvatar());
    }

    @Test
    public void getUserTeamUsingEmail() {
        api = new BitbucketCloudApi(apiUrl, getMockedCredentials("x.y@gmail.com"));
        BbOrg team = api.getOrg(USER_KEY);
        assertEquals(USER_KEY, team.getKey());
        assertEquals("Vivek Pandey", team.getName());
        assertEquals("https://bitbucket.org/account/" + USER_KEY + "/avatar/50/", team.getAvatar());
    }


    @Test
    public void getTeam() {
        BbOrg team = api.getOrg(TEAM_KEY);
        assertEquals(TEAM_KEY, team.getKey());
        assertEquals("Vivek's Team", team.getName());
        assertEquals("https://bitbucket.org/account/" + TEAM_KEY + "/avatar/50/", team.getAvatar());
    }

    @Test
    public void getRepos() {
        BbPage<BbRepo> repos = api.getRepos(TEAM_KEY, 1, 100);
        assertEquals(2, repos.getSize());
        assertEquals("pipeline-demo-test", repos.getValues().get(0).getSlug());
        assertEquals("emptyrepo", repos.getValues().get(1).getSlug());
    }

    @Test
    public void getRepo() {
        BbRepo repo = api.getRepo(TEAM_KEY, "pipeline-demo-test");
        assertEquals("pipeline-demo-test", repo.getSlug());
    }

    @Test
    public void getRepoContent() {
        BbBranch branch = api.getDefaultBranch(USER_KEY, "demo1");
        assertNotNull(branch);
        String content = api.getContent(USER_KEY, "demo1", "Jenkinsfile", branch.getLatestCommit());
        assertEquals("node{\n" +
                "  echo 'hello world!'\n" +
                "}", content);
    }


    @Test
    public void createNewRepoContent() {
        //create new file
        BbSaveContentResponse saveResponse = api.saveContent(USER_KEY, "demo1", "foo",
                "This is test content in new file",
                "first commit", "null", null, null);
        assertNotNull(saveResponse.getCommitId());
        String content = api.getContent(USER_KEY, "demo1", "foo", (String) saveResponse.getCommitId());
        assertEquals("This is test content in new file", content);
    }

    //This is duplicated code from server, we can't move to base class as inheritance won't work with @RunWith annotation
    //from subclasses
    private StandardUsernamePasswordCredentials getMockedCredentials() {
        return getMockedCredentials(getUserName());
    }

    private StandardUsernamePasswordCredentials getMockedCredentials(final String username) {
        final Secret secret = Mockito.mock(Secret.class);
        when(secret.getPlainText()).thenReturn(getPassword());

        PowerMockito.mockStatic(Secret.class);
        when(Secret.toString(secret)).thenReturn(getPassword());

        return new StandardUsernamePasswordCredentials() {
            @Override
            public CredentialsScope getScope() {
                return CredentialsScope.SYSTEM;
            }

            @NonNull
            @Override
            public CredentialsDescriptor getDescriptor() {
                return new CredentialsDescriptor() {

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

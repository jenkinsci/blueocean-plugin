package io.blueocean.ath.live;

import com.google.common.io.Resources;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.AthModule;
import io.blueocean.ath.Login;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.EditorPage;
import io.blueocean.ath.pages.blue.GithubCreationPage;
import io.blueocean.ath.sse.SSEClient;
import io.blueocean.ath.sse.SSEEvents;
import org.apache.log4j.Logger;
import org.jukito.UseModules;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHContentUpdateResponse;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.openqa.selenium.WebDriver;
import sun.jvm.hotspot.ui.Editor;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Properties;

@Login
@RunWith(ATHJUnitRunner.class)
@UseModules(AthModule.class)
public class GithubCreationTest{
    private Logger logger = Logger.getLogger(GithubCreationTest.class);

    private Properties props = new Properties();
    private String token;
    private String organization;
    private String repo;
    private Boolean deleteRepo = false;
    private Boolean randomSuffix = false;

    private GitHub github;
    private GHRepository ghRepository;
    @After
    public void deleteRepository() throws IOException {
        if(deleteRepo) {
            try {
                GHRepository repositoryToDelete = github.getRepository(organization + "/" + repo);
              //  repositoryToDelete.delete();
                logger.info("Deleted repository " + repo);
            } catch (FileNotFoundException e) {

            }
        }
    }

    @Before
    public void createBlankRepo() throws IOException {
        props.load(new FileInputStream("live.properties"));
        token = props.getProperty("github.token");
        organization = props.getProperty("github.org");
        repo = props.getProperty("github.repo");
        deleteRepo = Boolean.parseBoolean(props.getProperty("github.deleteRepo", "false"));
        randomSuffix = Boolean.parseBoolean(props.getProperty("github.randomSuffix", "false"));

        Assert.assertNotNull(token);
        Assert.assertNotNull(organization);
        Assert.assertNotNull(repo);

        logger.info("Loaded test properties");
        if(randomSuffix) {
            SecureRandom random = new SecureRandom();
            repo = repo + "-" + new BigInteger(50, random).toString(16);
        }

        github = GitHub.connectUsingOAuth(token);
        Assert.assertTrue(github.isCredentialValid());
        logger.info("Github credentials are valid");

        deleteRepository();

        ghRepository = github.createRepository(repo)
            .autoInit(true)
            .create();
        logger.info("Created repository " + repo);

    }



    @Inject
    GithubCreationPage creationPage;


    @Inject
    MultiBranchPipelineFactory mbpFactory;

    @Test
    public void testGithubCreation() throws IOException {
        URL jenkinsFileUrl = Resources.getResource(this.getClass(), "Jenkinsfile");
        byte[] content = Resources.toByteArray(jenkinsFileUrl);
        GHContentUpdateResponse updateResponse = ghRepository.createContent(content, "Jenkinsfile", "Jenkinsfile", "master");
        ghRepository.createRef("refs/heads/branch1", updateResponse.getCommit().getSHA1());
        logger.info("Created master and branch1 branches in " + repo);
        ghRepository.createContent("hi there","newfile", "newfile", "branch1");

        creationPage.createPipeline(token, organization, repo);
    }

    @Inject
    WebDriver driver;

    @Inject @Rule
    public SSEClient sseClient;

    @Inject EditorPage editorPage;
    @Test
    public void testEditor() throws IOException {
        creationPage.createPipeline(token, organization, repo, true);
        MultiBranchPipeline pipeline = mbpFactory.pipeline(Folder.folders(organization), repo);
        editorPage.simplePipeline();
        pipeline.getActivityPage().checkUrl();
        sseClient.untilEvents(SSEEvents.activityComplete(organization + "/" + repo));
    }
}

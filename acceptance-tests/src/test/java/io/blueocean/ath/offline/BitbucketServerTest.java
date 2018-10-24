package io.blueocean.ath.offline;

import com.cdancy.bitbucket.rest.BitbucketClient;
import com.cdancy.bitbucket.rest.options.CreateRepository;
import com.google.inject.Inject;
import io.blueocean.ath.*;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.blue.EditorPage;
import io.blueocean.ath.pages.blue.GithubCreationPage;
import io.jenkins.blueocean.util.HttpRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;

import java.io.IOException;

@Login
@RunWith(ATHJUnitRunner.class)
public class BitbucketServerTest implements WebDriverMixin {

    private static Logger logger = Logger.getLogger(BitbucketServerTest.class);
    private static final String ENDPOINT_URL = "http://127.0.0.1:7990";
    private static final String BB_PROJECT_KEY = "BLUE";
    private static final String BB_PROJECT_NAME = "BlueOcean";
    private static final String BB_REPO_NAME = "bitbucket-no-jenkinsfile";
    private static final String BB_USER_ID = "admin";
    private static final String BB_PASSWORD = "admin";

    @Inject
    WaitUtil wait;

    @Inject
    ActivityPage activityPage;

    @Inject
    DashboardPage dashboardPage;

    @Inject
    EditorPage editorPage;

    @Inject
    GithubCreationPage creationPage;

    @Inject @BaseUrl
    String baseUrl;

    @Inject
    CustomJenkinsServer jenkins;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    JenkinsUser jenkinsUser;

    @Before
    public void setUp() throws IOException {
        logger.info("--> setUp");
        cleanupEndpoint(ENDPOINT_URL);
        cleanupCredentials(ENDPOINT_URL);
        jobApi.deletePipeline(BB_REPO_NAME);
    }

    @Test
    @Retry(3)
    public void testCreationNoJenkinsfile() throws InterruptedException {
        BitbucketClient client = BitbucketClient.builder()
            .endPoint(ENDPOINT_URL)
            .credentials((BB_USER_ID + ":" + BB_PASSWORD)).build();

        client.api().repositoryApi().delete(BB_PROJECT_KEY, BB_REPO_NAME);
        client.api().repositoryApi().create(BB_PROJECT_KEY, CreateRepository.create(BB_REPO_NAME, true));

        dashboardPage.clickNewPipelineBtn();

        click("//span[text()='Bitbucket Server']");
        logger.info("Selected bitbucket server");

        click(".button-add-server");
        logger.info("Clicked addserver");

        wait.until(By.cssSelector(".text-name input")).sendKeys("bitbucketserver");
        wait.until(By.cssSelector(".text-url input")).sendKeys(ENDPOINT_URL);
        wait.click(By.cssSelector(".button-create-server"));
        wait.click(By.cssSelector(".button-next-step"));
        wait.sendKeys(By.cssSelector(".text-username input"),BB_USER_ID);
        wait.sendKeys(By.cssSelector(".text-password input"),BB_PASSWORD);
        wait.click(By.cssSelector(".button-create-credential"));
        logger.info("Bitbucket server created successfully");
        // Select project
        creationPage.selectOrganization(BB_PROJECT_NAME);
        creationPage.selectPipelineToCreate(BB_REPO_NAME);
        creationPage.clickCreatePipelineButton();
        editorPage.simplePipeline();
        editorPage.saveBranch("master");
        activityPage.checkBasicDomElements();
    }

    private void cleanupEndpoint(String endpointUrl) throws IOException {
        logger.info("Calling cleanupEndpoint to remove our Bitbucket server from Jenkins");
        String serverId = DigestUtils.sha256Hex(endpointUrl);

        try {
            httpRequest().Delete("/organizations/jenkins/scm/bitbucket-server/servers/{serverId}/")
                .urlPart("serverId", serverId)
                .status(204)
                .as(Void.class);
            logger.info("found and deleted bitbucket server: " + serverId);
        } catch (Exception ex) {
            logger.debug("server not found while attempting to delete Bitbucket server: " + serverId);
        }
    }

    private void cleanupCredentials(String endpointUrl) throws IOException {
        logger.info("Calling cleanupCredentials to remove the credentials we used with Bitbucket server");
        String serverId = DigestUtils.sha256Hex(endpointUrl);
        String credentialId = "bitbucket-server:" + serverId;
        jenkins.deleteUserDomainCredential(jenkinsUser.username, "blueocean-bitbucket-server-domain", credentialId);
    }

    private HttpRequest httpRequest() {
        return new HttpRequest(baseUrl + "/blue/rest");
    }
}

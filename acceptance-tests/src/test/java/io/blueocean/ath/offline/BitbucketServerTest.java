package io.blueocean.ath.offline;

import com.cdancy.bitbucket.rest.BitbucketClient;
import com.cdancy.bitbucket.rest.options.CreateRepository;
import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.CustomJenkinsServer;
import io.blueocean.ath.Login;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.pages.blue.DashboardPage;
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

    private static Logger LOGGER = Logger.getLogger(BitbucketServerTest.class);
    private static final String ENDPOINT_URL = "http://127.0.0.1:7990";
    private static final String BB_PROJECT_KEY = "BLUE";
    private static final String BB_PROJECT_NAME = "BlueOcean";
    private static final String BB_REPO_NAME = "bitbucket-no-jenkinsfile";

    @Inject
    WaitUtil wait;

    @Inject
    DashboardPage dashboardPage;

    @Inject
    GithubCreationPage creationPage;

    @Inject @BaseUrl
    String baseUrl;

    @Inject
    CustomJenkinsServer jenkins;

    @Inject
    ClassicJobApi jobApi;

    @Before
    public void setUp() throws IOException {
        cleanupEndpoint(ENDPOINT_URL);
        cleanupCredentials(ENDPOINT_URL);
        jobApi.deletePipeline(BB_REPO_NAME);
    }

    @Test
    public void testCreationNoJenkinsfile() throws InterruptedException {
        BitbucketClient client = BitbucketClient.builder()
            .endPoint(ENDPOINT_URL)
            .credentials("admin:admin").build();

        client.api().repositoryApi().delete(BB_PROJECT_KEY, BB_REPO_NAME);
        client.api().repositoryApi().create(BB_PROJECT_KEY, CreateRepository.create(BB_REPO_NAME, true));

        dashboardPage.clickNewPipelineBtn();

        click("//span[text()='Bitbucket Server']");
        LOGGER.info("Selected bitbucket server");

        click(".button-add-server");
        LOGGER.info("Clicked addserver");

        wait.until(By.cssSelector(".text-name input")).sendKeys("bitbucketserver");
        wait.until(By.cssSelector(".text-url input")).sendKeys(ENDPOINT_URL);
        click(".button-create-server");

        click(".button-next-step");

        wait.until(By.cssSelector(".text-username input")).sendKeys("admin");
        wait.until(By.cssSelector(".text-password input")).sendKeys("admin");
        click(".button-create-credental");

        creationPage.selectOrganization(BB_PROJECT_NAME);
        creationPage.selectPipelineToCreate(BB_REPO_NAME);
        creationPage.clickCreatePipelineButton();
    }

    private void cleanupEndpoint(String endpointUrl) throws IOException {
        String serverId = DigestUtils.sha256Hex(endpointUrl);

        try {
            httpRequest().Delete("/organizations/jenkins/scm/bitbucket-server/servers/{serverId}/")
                .urlPart("serverId", serverId)
                .status(204)
                .as(Void.class);
            LOGGER.info("found and deleted bitbucket server: " + serverId);
        } catch (Exception ex) {
            LOGGER.debug("server not found while attempting to delete bitbucket server: " + serverId);
        }
    }

    private void cleanupCredentials(String endpointUrl) throws IOException {
        String serverId = DigestUtils.sha256Hex(endpointUrl);
        String credentialId = "bitbucket-server:" + serverId;
        jenkins.deleteUserDomainCredential("alice", "blueocean-bitbucket-server-domain", credentialId);
    }

    private HttpRequest httpRequest() {
        return new HttpRequest(baseUrl + "/blue/rest");
    }
}

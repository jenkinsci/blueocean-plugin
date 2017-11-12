package io.blueocean.ath.offline;

import com.cdancy.bitbucket.rest.BitbucketClient;
import com.cdancy.bitbucket.rest.options.CreateRepository;
import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.Login;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

@Login
@RunWith(ATHJUnitRunner.class)
public class BitbucketServerTest implements WebDriverMixin {


    private static Logger LOGGER = Logger.getLogger(BitbucketServerTest.class);

    @Inject
    WaitUtil wait;

    @Inject
    DashboardPage dashboardPage;

    @Test
    public void testJenkinsfileCreate() throws InterruptedException {
        BitbucketClient client = BitbucketClient.builder()
            .endPoint("http://127.0.0.1:7990/")
            .credentials("admin:admin").build();

        String project = "BLUE";
        String repo = "testJenkinsfileCreate";
        client.api().repositoryApi().delete(project, repo);
        client.api().repositoryApi().create(project, CreateRepository.create(repo, true));

        dashboardPage.clickNewPipelineBtn();

        click("//span[text()='Bitbucket Server']");
        LOGGER.info("Selected bitbucket server");

        click(".button-add-server");
        LOGGER.info("Clicked addserver");

        wait.until(By.cssSelector(".text-name input")).sendKeys("bitbucketserver");
        wait.until(By.cssSelector(".text-url input")).sendKeys("http://127.0.0.1:7990");
        click(".button-create-server");

        click(".button-next-step");

        wait.until(By.cssSelector(".text-username input")).sendKeys("admin");
        wait.until(By.cssSelector(".text-password input")).sendKeys("admin");
        click(".button-create-credental");

    }
}

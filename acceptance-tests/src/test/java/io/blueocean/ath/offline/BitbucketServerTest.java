package io.blueocean.ath.offline;

import com.cdancy.bitbucket.rest.BitbucketClient;
import com.cdancy.bitbucket.rest.options.CreateRepository;
import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.Login;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;

@Login
@RunWith(ATHJUnitRunner.class)
public class BitbucketServerTest {

    private static Logger LOGGER = Logger.getLogger(BitbucketServerTest.class);

    @Inject
    WaitUtil wait;

    @Inject
    DashboardPage dashboardPage;

    @Test
    public void testJenkinsfileCreate() {
        BitbucketClient client = BitbucketClient.builder()
            .endPoint("http://127.0.0.1:7990/")
            .credentials("admin:admin").build();

        String project = "BLUE";
        String repo = "testJenkinsfileCreate";
        client.api().repositoryApi().delete(project, repo);
        client.api().repositoryApi().create(project, CreateRepository.create(repo, true));

        dashboardPage.clickNewPipelineBtn();

        wait.until(By.xpath("//span[text()='Bitbucket Server']")).click();
        LOGGER.info("Selected bitbucket server");

        wait.until(By.xpath("//span[text()='Bitbucket Server']")).click();
        LOGGER.info("Selected bitbucket server");

        wait.until(By.cssSelector(".button-add-server")).click();
        LOGGER.info("Clicked addserver");

        wait.until(By.xpath("//input[@placeholder='My Bitbucket Server']")).sendKeys("bitbucketserver");
        wait.until(By.xpath("//input[@placeholder='https://mybitbucket.example.com']")).sendKeys("http://127.0.0.1:7990");
        wait.until(By.cssSelector(".button-create-server")).click();
        wait.until(By.cssSelector(".button-next-step")).click();
        LOGGER.info("Entered server details");

        wait.until(By.xpath("(//input[ @class='TextInput-control'])[1]")).sendKeys("admin");
        wait.until(By.xpath("(//input[ @class='TextInput-control'])[2]")).sendKeys("admin");
        wait.until(By.cssSelector(".button-create-credental")).click();
        LOGGER.info("Entered credentials");
    }
}

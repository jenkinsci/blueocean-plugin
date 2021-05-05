package io.blueocean.ath.pages.blue;

import io.blueocean.ath.AcceptanceTestException;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.sse.SSEClientRule;
import io.blueocean.ath.sse.SSEEvents;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class GitCreationPage {
    private Logger logger = Logger.getLogger(GitCreationPage.class);

    private WebDriver driver;

    @Inject
    WaitUtil wait;

    @Inject
    DashboardPage dashboardPage;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    MultiBranchPipelineFactory multiBranchPipelineFactory;

    @Inject
    public GitCreationPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public GitCreationPage clickGitCreationOption() {
        wait.until(By.cssSelector(".scm-provider-list .git-creation")).click();
        logger.info("Selected git creation");
        return this;
    }

    public MultiBranchPipeline createPipelineSSH(SSEClientRule sseCLient, String pipelineName, String url, String sshPrivateKey) throws IOException {
        jobApi.deletePipeline(pipelineName);
        dashboardPage.clickNewPipelineBtn();
        clickGitCreationOption();
        wait.until(By.cssSelector("div.text-repository-url input")).sendKeys(url);
        wait.until(By.cssSelector("button.button-create-credential")).click();

        wait.until(By.xpath("//span[text()='SSH Key']")).click();
        wait.until(By.cssSelector("textarea.TextArea-control")).sendKeys(sshPrivateKey);
        wait.until(By.cssSelector(".Dialog-button-bar button.button-create-credential")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".create-credential-dialog")));
        logger.info("Created credential");

        wait.until(By.cssSelector(".button-create-pipeline")).click();
        logger.info("Click create pipeline button");

        MultiBranchPipeline pipeline = multiBranchPipelineFactory.pipeline(pipelineName);
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/activity"), 30000);
        sseCLient.untilEvents(SSEEvents.activityComplete(pipeline.getName()));
        driver.navigate().refresh();
        pipeline.getActivityPage().checkUrl();
        return pipeline;
    }

    public MultiBranchPipeline createPipelinePW(SSEClientRule sseCLient, String pipelineName, String url, String user, String pass) throws IOException {
        jobApi.deletePipeline(pipelineName);
        dashboardPage.clickNewPipelineBtn();
        clickGitCreationOption();
        wait.until(By.cssSelector("div.text-repository-url input")).sendKeys(url);

        wait.until(By.xpath("//*[contains(text(), 'Jenkins needs a user credential')]"));

        boolean createCredentialFound = false;
        try{
            wait.until(By.xpath("//*[contains(text(), 'Create new credential')]"));
            createCredentialFound = true;
        } catch ( NoSuchElementException|TimeoutException|AcceptanceTestException e ){
            // ignore it
        }
        if(createCredentialFound){
            driver.findElement(By.xpath( "//*[contains(text(),'Create new credential')]")).click();
        }

        wait.until(By.cssSelector("div.text-username input")).sendKeys(user);
        wait.until(By.cssSelector("div.text-password input")).sendKeys(pass);
        wait.until(By.cssSelector(".button-create-credential")).click();

        wait.until(By.xpath("//*[contains(text(), 'Use existing credential')]"));

        logger.info("Created user/pass credential");
        wait.until(By.cssSelector(".button-create-pipeline")).click();
        logger.info("Click create pipeline button");

        MultiBranchPipeline pipeline=null;
        try {
            pipeline = multiBranchPipelineFactory.pipeline(pipelineName);
            String urlPart = pipeline.getUrl() + "/activity";
            logger.info("waiting for urlPart: " + urlPart);
            wait.until(ExpectedConditions.urlContains(urlPart), 30000);
            sseCLient.untilEvents(SSEEvents.activityComplete(pipeline.getName()));
            driver.navigate().refresh();
            pipeline.getActivityPage().checkUrl();
            return pipeline;
        } finally {
            deleteQuietly(pipeline, pipelineName);
        }
    }

    private void deleteQuietly(MultiBranchPipeline pipeline, String pipelineName){
        try{
            pipeline.deleteThisPipeline(pipelineName);
        } catch (Throwable e){
            //
        }
    }
}

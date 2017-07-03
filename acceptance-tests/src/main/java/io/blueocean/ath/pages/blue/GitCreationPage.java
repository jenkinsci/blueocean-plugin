package io.blueocean.ath.pages.blue;

import com.google.common.base.Strings;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.factory.PipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.model.Pipeline;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
        wait.until(By.xpath("//span[text()='Git']")).click();
        logger.info("Selected git creation");
        return this;
    }

    public MultiBranchPipeline createPipeline(String pipelineName, String url, String sshPrivateKey, String user, String pass) throws IOException {
        jobApi.deletePipeline(pipelineName);
        dashboardPage.clickNewPipelineBtn();
        clickGitCreationOption();
        wait.until(By.cssSelector("div.text-repository-url input")).sendKeys(url);
        wait.until(By.cssSelector("button.button-create-credential")).click();

        if(!Strings.isNullOrEmpty(sshPrivateKey)) {
            wait.until(By.xpath("//span[text()='SSH Key']")).click();
            wait.until(By.cssSelector("textarea.TextArea-control")).sendKeys(sshPrivateKey);
            wait.until(By.cssSelector(".Dialog-button-bar button.button-create-credental")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".create-credential-dialog")));
            logger.info("Created credential");
        } else if(!Strings.isNullOrEmpty(user) && !Strings.isNullOrEmpty(pass)) {
            wait.until(By.xpath("//span[text()='Username & Password']")).click();
            wait.until(By.cssSelector("div.text-username input")).sendKeys(user);
            wait.until(By.cssSelector("div.text-password input")).sendKeys(pass);
            wait.until(By.cssSelector(".Dialog-button-bar button.button-create-credental")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".create-credential-dialog")));
            logger.info("Created user/pass credential");
        }

        wait.until(By.cssSelector(".button-create-pipeline")).click();
        logger.info("Click create pipeline button");

        MultiBranchPipeline pipeline = multiBranchPipelineFactory.pipeline(pipelineName);
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/activity"), 30000);
        driver.navigate().refresh();
        pipeline.getActivityPage().checkUrl();
        return pipeline;
    }
}

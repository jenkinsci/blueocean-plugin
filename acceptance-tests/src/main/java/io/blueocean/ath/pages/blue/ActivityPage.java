package io.blueocean.ath.pages.blue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.Pipeline;
import org.apache.log4j.Logger;
import org.eclipse.jgit.annotations.Nullable;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;

public class ActivityPage {
    private Logger logger = Logger.getLogger(ActivityPage.class);

    private WebDriver driver;
    private Pipeline pipeline;
    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    public ActivityPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @AssistedInject
    public ActivityPage(WebDriver driver, @Assisted @Nullable Pipeline pipeline) {
        this.pipeline = pipeline;
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void open(String pipeline) {
        driver.get(base+"/blue/organizations/jenkins/"+ pipeline + "/activity");
        logger.info("Opened activity page for " + pipeline);
    }

    public void checkPipeline() {
        Assert.assertNotNull("Pipeline is null", pipeline);
    }

    public void checkUrl() {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/activity"), 30000);
    }
    public void open() {
        checkPipeline();
        driver.get(pipeline.getUrl() + "/activity");
        wait.until(ExpectedConditions.urlToBe(pipeline.getUrl() + "/activity/"));
        logger.info("Opened activity page for " + pipeline);
    }
    public void checkForCommitMesssage(String message) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()=\""+ message +"\"]")));
        logger.info("Found commit message '" + message + "'");
    }




}

package io.blueocean.ath.pages.blue;


import com.google.inject.assistedinject.Assisted;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.AbstractPipeline;
import java.net.URLEncoder;
import javax.inject.Inject;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class  RunDetailsArtifactsPage {
    private Logger logger = LoggerFactory.getLogger( RunDetailsArtifactsPage.class);

    private WebDriver driver;

    private AbstractPipeline pipeline;

    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    public RunDetailsArtifactsPage(WebDriver driver, @Assisted AbstractPipeline pipeline) {
        this.driver = driver;
        this.pipeline = pipeline;
        PageFactory.initElements(driver, this);
    }

    public void checkPipeline() {
        Assert.assertNotNull("AbstractPipeline is null", pipeline);
    }

    public void checkUrl(int runNumber) {
        checkUrl(null, runNumber);
    }
    public void checkUrl(String branch, int runNumber) {
        wait.until(ExpectedConditions.urlContains(getUrl(branch, runNumber)), 30000);
    }
    public String getUrl(int runNumber) {
        return getUrl(null, runNumber);
    }

    public String getUrl(String branch, int runNumber) {
        if(pipeline.isMultiBranch()) {
            String tempBranch = branch == null ? "master" : URLEncoder.encode(branch);
            return pipeline.getUrl() + "/detail/" + tempBranch + "/" + runNumber +"/artifacts";
        }

        return pipeline.getUrl() + "/detail/master/" + runNumber +"/artifacts";
    }

    public RunDetailsArtifactsPage open(String branch, int runNumber) {
        checkPipeline();
        driver.get(getUrl(branch, runNumber));
        checkUrl(branch, runNumber);
        logger.info("Opened RunDetailsArtifacts page for " + pipeline.getName());
        return this;
    }

    public RunDetailsArtifactsPage open(int runNumber) {
        return open(null, runNumber);
    }

    public RunDetailsArtifactsPage checkNumberOfArtifacts(int expected) {
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".artifacts-table .JTable-row"),
                expected+1)); //1 for heading
        return this;
    }

}

package io.blueocean.ath.pages.blue;

import com.google.inject.assistedinject.Assisted;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.AbstractPipeline;
import java.net.URLEncoder;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class  RunDetailsFakePage {
    private Logger logger = Logger.getLogger(RunDetailsFakePage.class);

    private WebDriver driver;

    private AbstractPipeline pipeline;

    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    public RunDetailsFakePage(WebDriver driver, @Assisted AbstractPipeline pipeline) {
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
            return pipeline.getUrl() + "/detail/" + tempBranch + "/" + runNumber +"/fakerundetails";
        }

        return pipeline.getUrl() + "/detail/master/" + runNumber +"/fakerundetails";
    }

    public RunDetailsFakePage open(String branch, int runNumber) {
        checkPipeline();
        driver.get(getUrl(branch, runNumber));
        checkUrl(branch, runNumber);
        logger.info("Opened RunDetailsFakePage page for " + pipeline.getName());
        return this;
    }

    public RunDetailsFakePage open(int runNumber) {
        return open(null, runNumber);
    }

}

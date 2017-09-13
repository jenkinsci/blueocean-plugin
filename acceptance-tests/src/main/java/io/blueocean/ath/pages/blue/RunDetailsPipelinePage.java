package io.blueocean.ath.pages.blue;


import com.google.inject.assistedinject.Assisted;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.AbstractPipeline;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;

public class RunDetailsPipelinePage {
    private Logger logger = Logger.getLogger(RunDetailsPipelinePage.class);

    private WebDriver driver;

    private AbstractPipeline pipeline;

    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    public RunDetailsPipelinePage(WebDriver driver, @Assisted AbstractPipeline pipeline) {
        this.driver = driver;
        this.pipeline = pipeline;
        PageFactory.initElements(driver, this);
    }

    public void open(String pipeline, Integer runNumber) {
        driver.get(base+"/blue/organizations/jenkins/"+ pipeline + "/detail/master/" + runNumber +"/pipeline");
        logger.info("Opened result page for " + pipeline);
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
            String tempBranch = branch == null ? "master" : branch;
            return pipeline.getUrl() + "/detail/" + tempBranch + "/" + runNumber +"/pipeline";
        }

        return pipeline.getUrl() + "/detail/master/" + runNumber +"/pipeline";
    }

    public RunDetailsPipelinePage open(String branch, int runNumber) {
        checkPipeline();
        driver.get(getUrl(branch, runNumber));
        checkUrl(branch, runNumber);
        logger.info("Opened RunDetailsPipeline page for " + pipeline.getName());
        return this;
    }

    public RunDetailsPipelinePage open(int runNumber) {
        return open(null, runNumber);
    }

}

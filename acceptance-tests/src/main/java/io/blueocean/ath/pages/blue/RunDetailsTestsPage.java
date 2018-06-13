package io.blueocean.ath.pages.blue;


import com.google.inject.assistedinject.Assisted;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.model.AbstractPipeline;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;

public class RunDetailsTestsPage implements WebDriverMixin {
    private Logger logger = Logger.getLogger(RunDetailsTestsPage.class);

    private WebDriver driver;

    private AbstractPipeline pipeline;

    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    public RunDetailsTestsPage(WebDriver driver, @Assisted AbstractPipeline pipeline) {
        this.driver = driver;
        this.pipeline = pipeline;
        PageFactory.initElements(driver, this);
    }

    public void open(String pipeline, Integer runNumber) {
        driver.get(base+"/blue/organizations/jenkins/"+ pipeline + "/detail/master/" + runNumber +"/tests");
        logger.info("Opened result page for " + pipeline);
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
            return pipeline.getUrl() + "/detail/" + tempBranch + "/" + runNumber +"/tests";
        }

        return pipeline.getUrl() + "/detail/master/" + runNumber +"/tests";
    }

    public RunDetailsTestsPage open(String branch, int runNumber) {
        driver.get(getUrl(branch, runNumber));
        checkUrl(branch, runNumber);
        logger.info("Opened RunDetailsPipeline page for " + pipeline.getName());
        return this;
    }

    public RunDetailsTestsPage open(int runNumber) {
        return open(null, runNumber);
    }

    public RunDetailsTestsPage checkResults(String type, int number) {
        // check for logs for freestyle job or for pipeline job
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("div.result-item."+type), number));
        return this;
    }

    public WaitUtil getWaitUntil() {
        return wait;
    }

}

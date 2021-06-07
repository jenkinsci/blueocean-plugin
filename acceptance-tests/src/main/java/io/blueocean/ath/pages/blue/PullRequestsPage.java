package io.blueocean.ath.pages.blue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.model.AbstractPipeline;
import org.eclipse.jgit.annotations.Nullable;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import javax.inject.Inject;

public class PullRequestsPage implements WebDriverMixin {
    private Logger logger = LoggerFactory.getLogger(PullRequestsPage.class);

    private WebDriver driver;
    private AbstractPipeline pipeline;

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    RunDetailsPipelinePageFactory runDetailsPipelinePageFactory;

    @Inject
    WaitUtil wait;

    @Inject
    public PullRequestsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @AssistedInject
    public PullRequestsPage(WebDriver driver, @Assisted @Nullable AbstractPipeline pipeline) {
        this.pipeline = pipeline;
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    /**
     * Validates a correctly constructed URL.
     * @return
     */
    public PullRequestsPage checkUrl() {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/pr"), 30000);
        logger.info("checkUrl: successfully validated our URL");
        return this;
    }

    /**
     * Validates that the pipeline object actually exists and can do stuff.
     * @return
     */
    public void checkPipeline() {
        Assert.assertNotNull("AbstractPipeline is null", pipeline);
    }

    /**
     * Verifies that the PR tab in the UI is actually selected.
     * @return
     */
    public void checkPr() {
        wait.until(By.cssSelector("a.selected.pr"));
        logger.info("checkPr: success, PR tab is selected");
    }

    /**
     * Clicks the History button for the specified PR number, taking the
     * test into that PR's Activity page.
     * @param prNumber
     * @return
     */
    public ActivityPage clickHistoryButton(String prNumber) {
        wait.click(By.cssSelector("a[data-pr='" + prNumber + "'] a.history-button"));
        logger.info("Clicked history button, which moves us to the Activity page");
        return activityPageFactory.withPipeline(pipeline).checkUrl(("PR-" + prNumber));
    }

    /**
     * Clicks the Run button for the specified PR number, taking the
     * test into that PR's Run Details page.
     * @param prNumber
     * @return
     */
    public RunDetailsPipelinePage clickRunButton(String prNumber) {
        wait.click(By.cssSelector("a[data-pr='" + prNumber + "'] a.run-button"));
        logger.info("Clicked Run button to build the PR");
        return runDetailsPipelinePageFactory.withPipeline(pipeline);
    }

    /**
     * Opens a PullRequestsPage for the specified pipeline
     * @param pipelineName
     * @return
     */
    public void open(String pipelineName) {
        checkPipeline();
        checkUrl();
        go("/blue/organizations/jenkins/" + pipelineName + "/pr");
        checkPr();
        logger.info("PullRequestsPage --> opened PR tab for " + pipelineName);
    }

    /**
     * Open run details for the specified pr (by clicking on its row)
     * @param prNumber
     * @return
     */
    public RunDetailsPipelinePage openPrDetails(String prNumber) {
        findPrRow(prNumber).click();
        return runDetailsPipelinePageFactory.withPipeline(pipeline);
    }

    /**
     * Locate the row for the specified pr
     * @param prNumber
     * @return
     */
    private WebElement findPrRow(String prNumber) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-pr='" + prNumber + "']")));
    }

}

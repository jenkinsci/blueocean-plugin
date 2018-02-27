package io.blueocean.ath.pages.blue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.Locate;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.PullRequestsPageFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.model.AbstractPipeline;
import org.apache.log4j.Logger;
import org.eclipse.jgit.annotations.Nullable;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;

public class PullRequestsPage implements WebDriverMixin {
    private Logger logger = Logger.getLogger(BranchPage.class);

    private WebDriver driver;
    private AbstractPipeline pipeline;

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    PullRequestsPageFactory pullRequestsPageFactory;

    @Inject
    RunDetailsPipelinePageFactory runDetailsPipelinePageFactory;

    @Inject
    WaitUtil wait;

    @Inject
    EditorPage editorPage;

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

    public PullRequestsPage checkForNoPullRequestsAvailable () {
        logger.info("--> This should look for the \"you don\'t have any pull requests\" thing");
        return this;
    }

    public PullRequestsPage checkUrl() {
        // The AbstractPipeline object `pipeline` is null, and this still passes.
        // Need to figure out what I'm doing wrong with passing around the pipeline object.
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/pr"), 30000);
        // wait.until(By.cssSelector("a.selected.pr"));
        logger.info("checkUrl: success, PR Tab is selected");
        return this;
    }

    public void checkPipeline() {
        Assert.assertNotNull("AbstractPipeline is null", pipeline);
    }

    public void checkPr() {
        wait.until(By.cssSelector("a.selected.pr"));
        logger.info("checkPr: success, PR tab is selected");
    }

    public ActivityPage clickHistoryButton(String prNumber) {
        // Ryan helped me fix this.
        wait.click(By.cssSelector("a[data-pr='" + prNumber + "'] a.history-button"));
        logger.info("Clicked history button and moving to Activity page");
        return activityPageFactory.withPipeline(pipeline).checkUrl(("PR-" + prNumber));
    }

    public PullRequestsPage clickRow(String commitMessage) {
        wait.click(By.cssSelector("div[JTable-cell-contents='" + commitMessage + "']"));
        return pullRequestsPageFactory.withPipeline(pipeline).checkUrl();
    }

    public RunDetailsPipelinePage clickRunButton(String prNumber) {
        wait.click(By.cssSelector("a[data-pr='" + prNumber + "'] a.run-button"));
        logger.info("Clicked Run button to build the PR");
        // return activityPageFactory.withPipeline(pipeline).checkUrl(("PR-" + prNumber));
        // String runNumber = driver.findElement(By.cssSelector("span[data-pr='\" + prNumber + \"'] a.run-button"));
        return runDetailsPipelinePageFactory.withPipeline(pipeline);
    }


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

    private WebElement findPrRow(String prNumber) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-pr='" + prNumber + "']")));
    }

}

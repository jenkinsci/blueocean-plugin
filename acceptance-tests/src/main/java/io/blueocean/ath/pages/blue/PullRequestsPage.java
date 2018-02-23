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


    /**
    HEY
    Look for something like multibranch-table, pr-table, or article-table

    Here's the one from BranchPage:
    public BranchPage checkUrl() {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/branches"), 30000);
        wait.until(By.cssSelector("div.multibranch-table"));
        return this;
    }
    Here's the one from ActivityPage:
    public ActivityPage checkUrl() {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/activity"), 120000);
        wait.until(By.cssSelector("article.activity"), 60000);
        return this;
    }
    */
    public PullRequestsPage checkUrl() {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/pr"), 30000);
        // HEY
        // Look for
        wait.until(By.cssSelector("div.multibranch-table"));
        return this;
    }

    /**
     * HEY
     *
     * Add a checkPipeline method maybe? Or checkPr?
     *
     *
     *
     */
    public void checkPipeline() {
        Assert.assertNotNull("AbstractPipeline is null", pipeline);
    }

    public PullRequestsPage clickHistoryButton(String prNumber) {
        wait.until(By.cssSelector("div[data-branch='" + prNumber + "'] a.history-button")).click();
        logger.info("Clicked history button of branch " + prNumber);
        // return pullRequestsPageFactory.withPipeline(pipeline).checkUrl(branch);
        return pullRequestsPageFactory.withPipeline(pipeline).checkUrl();
    }

    public void open(String pipelineName) {
        go("/blue/organizations/jenkins/" + pipelineName + "/pr");
        logger.info("PullRequestsPage --> opened PR tab for " + pipelineName);
    }

    public EditorPage openEditor(String branch) {
        wait.until(By.cssSelector("div[data-branch='" + branch + "'] a.pipeline-editor-link")).click();
        logger.info("Clicked Editor button of branch " + branch);
        return editorPage;
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

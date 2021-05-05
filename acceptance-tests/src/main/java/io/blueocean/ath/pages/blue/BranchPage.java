package io.blueocean.ath.pages.blue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.Locate;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.model.AbstractPipeline;
import org.eclipse.jgit.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class BranchPage implements WebDriverMixin {
    private Logger logger = LoggerFactory.getLogger(BranchPage.class);

    private WebDriver driver;
    private AbstractPipeline pipeline;

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    RunDetailsPipelinePageFactory runDetailsPipelinePageFactory;

    @Inject
    WaitUtil wait;

    @Inject
    EditorPage editorPage;

    @Inject
    public BranchPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @AssistedInject
    public BranchPage(WebDriver driver, @Assisted @Nullable AbstractPipeline pipeline) {
        this.pipeline = pipeline;
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public BranchPage checkUrl() {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/branches"), 30000);
        wait.until(By.cssSelector("div.multibranch-table"));
        return this;
    }

    public ActivityPage clickActivityTab() {
        wait.click(By.cssSelector("a.activity"));
        logger.info("Clicked Activity tab");
        return activityPageFactory.withPipeline(pipeline).checkUrl();
    }

    public ActivityPage clickHistoryButton(String branch) {
        wait.until(By.cssSelector("div[data-branch='" + branch + "'] a.history-button")).click();
        logger.info("Clicked history button of branch {}", branch);
        return activityPageFactory.withPipeline(pipeline).checkUrl(branch);
    }

    public BranchPage clickRunButton(String branch) {
        wait.click(By.cssSelector("div[data-branch='" + branch + "'] a.run-button"));
        logger.info("Clicked run button of branch {}", branch);
        return this;
    }

    public BranchPage clickStopButton(String branch) {
        wait.click(By.cssSelector("div[data-branch='" + branch + "'] a.stop-button"));
        logger.info("Clicked stop button of branch {}", branch);
        return this;
    }

    public BranchPage open() {
        driver.get(pipeline.getUrl() + "/branches");
        checkUrl();
        logger.info("Opened branch page for {}", pipeline);
        return this;
    }

    public EditorPage openEditor(String branch) {
        wait.until(By.cssSelector("div[data-branch='" + branch + "'] a.pipeline-editor-link")).click();
        logger.info("Clicked Editor button of branch {}", branch);
        return editorPage;
    }

    /**
     * Check whether the specified branch is favorited (or not)
     * @param branchName
     * @param isFavorite
     * @return builder
     */
    public BranchPage checkFavoriteStatus(String branchName, boolean isFavorite) {
        WebElement favorite = findBranchRow(branchName).findElement(By.cssSelector(".Favorite input"));
        wait.until(ExpectedConditions.elementSelectionStateToBe(favorite, isFavorite));
        return this;
    }

    /**
     * Toggle the favorite status for specified branch
     * @param branchName
     * @return builder
     */
    public BranchPage toggleFavoriteStatus(String branchName) {
        WebElement favorite = findBranchRow(branchName).findElement(By.cssSelector(".Favorite label"));
        wait.click(Locate.byElem(favorite));
        return this;
    }

    /**
     * Open run details for the specified branch (by clicking on its row)
     * @param branchName
     * @return
     */
    public RunDetailsPipelinePage openRunDetails(String branchName) {
        findBranchRow(branchName).click();
        return runDetailsPipelinePageFactory.withPipeline(pipeline);
    }

    private WebElement findBranchRow(String branchName) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-branch='" + branchName + "']")));
    }

}

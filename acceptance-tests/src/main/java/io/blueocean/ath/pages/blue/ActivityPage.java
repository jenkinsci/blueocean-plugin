package io.blueocean.ath.pages.blue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.factory.BranchPageFactory;
import io.blueocean.ath.factory.PullRequestsPageFactory;
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
import java.net.URLEncoder;

public class ActivityPage {
    private Logger logger = Logger.getLogger(ActivityPage.class);

    private WebDriver driver;
    private AbstractPipeline pipeline;
    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    BranchPageFactory branchPageFactory;

    @Inject PullRequestsPageFactory pullRequestsPageFactory;

    @Inject
    public ActivityPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @AssistedInject
    public ActivityPage(WebDriver driver, @Assisted @Nullable AbstractPipeline pipeline) {
        this.pipeline = pipeline;
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @Deprecated
    public void open(String pipeline) {
        driver.get(base + "/blue/organizations/jenkins/" + pipeline + "/activity");
        logger.info("Opened activity page for " + pipeline);
    }

    public void checkPipeline() {
        Assert.assertNotNull("AbstractPipeline is null", pipeline);
    }

    public ActivityPage checkUrl() {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/activity"), 120000);
        wait.until(By.cssSelector("article.activity"), 60000);
        return this;
    }

    public ActivityPage checkUrl(String filter) {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/activity?branch=" + URLEncoder.encode(URLEncoder.encode(filter))), 30000);
        wait.until(By.cssSelector("article.activity"), 60000);
        return this;
    }

    public ActivityPage open() {
        checkPipeline();
        driver.get(pipeline.getUrl() + "/activity");
        checkUrl();
        logger.info("Opened activity page for " + pipeline);
        return this;
    }

    public void checkForCommitMessage(String message) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()=\"" + message + "\"]")));
        logger.info("Found commit message '" + message + "'");
    }

    public BranchPage clickBranchTab() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.branches"))).click();
        logger.info("Clicked on branch tab");
        // return branchPageFactory.withPipeline(pipeline).checkUrl();
        BranchPage page = branchPageFactory.withPipeline(pipeline);
        Assert.assertNotNull("AbstractPipeline object is null", page);
        return page.checkUrl();
    }

    public PullRequestsPage clickPullRequestsTab() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.pr"))).click();
        logger.info("Clicked on PR tab");
        PullRequestsPage page = pullRequestsPageFactory.withPipeline(pipeline);
        Assert.assertNotNull("AbstractPipeline object is null", page);
        return page.checkUrl();
    }

    public By getSelectorForBranch(String branchName) {
        return By.xpath("//*[@data-branch=\"" + branchName + "\"]");
    }

    public WebElement getRunRowForBranch(String branchName) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(getSelectorForBranch(branchName)));
    }

    public By getSelectorForRowCells() {
        return By.className("JTable-cell");
    }

    public void assertIsDuration(String text) {
        final String durationRegex = "<1s|\\d+\\w";
        Assert.assertTrue("String (\"" + text + "\") contains a valid duration", text.matches(durationRegex));
    }

    public void testNumberRunsComplete(int atLeast) {
        testNumberRunsComplete(atLeast, "success");
        By selector = By.cssSelector("div[data-pipeline='" + pipeline.getName() + "'].JTable-row circle.success");
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(selector, atLeast - 1));
        logger.info("At least " + atLeast + " runs are complete");
    }

    public void testNumberRunsComplete(int atLeast, String status) {
        By selector = By.cssSelector("div[data-pipeline='" + pipeline.getName() + "'].JTable-row circle."+status);
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(selector, atLeast - 1));
        logger.info("At least " + atLeast + " runs are complete");
    }

    public void checkBasicDomElements() {
        wait.retryAction("check that we are on the activity page", 3, driver -> {
            wait.until(By.cssSelector("article.activity"), 5000);
            logger.info("checkBasicDomElements: Activity tab found");
            return true;
        });
    }

    public void checkFavoriteStatus(boolean isFavorited) {
        wait.until(driver -> {
            WebElement favorite = driver.findElement(By.cssSelector(".Favorite.Checkbox input"));
            return isFavorited == favorite.isSelected();
        });
    }

    public void toggleFavorite() {
        wait.until(driver -> {
            WebElement favorite = driver.findElement(By.cssSelector(".Favorite.Checkbox label"));
            favorite.click();
            return favorite;
        });
    }
}

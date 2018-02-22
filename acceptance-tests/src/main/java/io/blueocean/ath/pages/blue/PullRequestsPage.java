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

import static io.blueocean.ath.factory.PullRequestsPageFactory.*;

// I should have started with branchPage.


public class PullRequestsPage {
    private Logger logger = Logger.getLogger(ActivityPage.class);

    private WebDriver driver;
    private AbstractPipeline pipeline;
    @Inject
    @BaseUrl
    String base;

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

    /*
    public void checkPipeline(AbstractPipeline pipeline) {
        Assert.assertNotNull("AbstractPipeline is null", pipeline);
    }
    */

    public PullRequestsPage checkUrl() {
        logger.info("--> It will be " + pipeline.getUrl() + "/pr");
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/pr"), 120000);
        wait.until(By.cssSelector("article.activity"), 60000);
        return this;
    }

    // public void open(String pipeline) {
    public PullRequestsPage open(String pipeline) {
        // checkPipeline(pipeline);
        driver.get(base + "/blue/organizations/jenkins/" + pipeline + "/pr");
        // checkUrl();
        logger.info("Opened PR page for " + pipeline);
        return this;
    }

    public PullRequestsPage clickBranchTab() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.branches"))).click();
        logger.info("Clicked on branch tab");
        return this.checkUrl();
    }

    public By getSelectorForBranch(String branchName) {
        return By.xpath("//*[@data-branch=\"" + branchName + "\"]");
    }

    public WebElement getRunRowForBranch(String branchName) {
        return wait.until(getSelectorForBranch(branchName));
    }

    public By getSelectorForRowCells() {
        return By.className("JTable-cell");
    }
}

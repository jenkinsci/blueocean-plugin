package io.blueocean.ath.pages.blue;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.model.Pipeline;
import org.apache.log4j.Logger;
import org.eclipse.jgit.annotations.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;

public class BranchPage {
    private Logger logger = Logger.getLogger(BranchPage.class);

    private WebDriver driver;
    private Pipeline pipeline;

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    public BranchPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @AssistedInject
    public BranchPage(WebDriver driver, @Assisted @Nullable Pipeline pipeline) {
        this.pipeline = pipeline;
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public BranchPage checkUrl() {
        wait.until(ExpectedConditions.urlContains(pipeline.getUrl() + "/branches"), 30000);
        wait.until(By.cssSelector("div.multibranch-table"));
        return this;
    }

    public ActivityPage clickHistoryButton(String branch) {
        wait.until(By.cssSelector("div[data-branch='" + branch + "'] a.history-button")).click();
        logger.info("Clicked history button of branch " + branch);
        return activityPageFactory.withPipeline(pipeline).checkUrl(branch);
    }
}

package io.blueocean.ath.pages.blue;

import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExecutorPluginPage {
    private Logger logger = LoggerFactory.getLogger(DashboardPage.class);

    @Inject
    @BaseUrl
    String base;

    @Inject
    public ExecutorPluginPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    @Inject
    public WaitUtil wait;

    @Inject
    public WebDriver driver;

    public void open() {
        driver.get(base + "/blue/executor-info");
        logger.info("Navigated to executor page");
    }

    public void checkComputers() {
            wait.until(ExpectedConditions.visibilityOfElementLocated(getSelectorExecutor()));


    }

    public By getSelectorExecutor() {
        return By.cssSelector(".executor-info-cell");
    }





}

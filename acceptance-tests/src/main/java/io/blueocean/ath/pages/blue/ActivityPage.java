package io.blueocean.ath.pages.blue;

import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ActivityPage {
    private Logger logger = Logger.getLogger(ActivityPage.class);

    private WebDriver driver;

    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    public ActivityPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void open(String pipeline) {
        driver.get(base+"/blue/organizations/jenkins/"+ pipeline + "/activity");
        logger.info("Opened activity page for " + pipeline);
    }

    public void checkForCommitMesssage(String message) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()=\""+ message +"\"]")));
        logger.info("Found commit message '" + message + "'");
    }




}

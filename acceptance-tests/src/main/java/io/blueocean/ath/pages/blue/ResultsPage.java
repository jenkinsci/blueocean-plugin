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
public class ResultsPage {
    private Logger logger = Logger.getLogger(ResultsPage.class);

    private WebDriver driver;

    @Inject
    @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    public ResultsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void open(String pipeline, Integer runNumber) {
        driver.get(base+"/blue/organizations/jenkins/"+ pipeline + "/detail/master/" + runNumber +"/pipeline");
        logger.info("Opened result page for " + pipeline);
    }


}

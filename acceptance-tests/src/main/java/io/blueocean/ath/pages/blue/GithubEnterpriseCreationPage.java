package io.blueocean.ath.pages.blue;

import io.blueocean.ath.WaitUtil;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GithubEnterpriseCreationPage extends GithubCreationPage {
    private Logger logger = Logger.getLogger(GithubEnterpriseCreationPage.class);

    @Inject
    public GithubEnterpriseCreationPage(WebDriver driver) {
        super(driver);
    }

    @Inject
    WaitUtil wait;

    @Inject
    WebDriver driver;

    @FindBy(css = "button.github-enterprise-creation")
    WebElement githubEnterpriseCreationButton;

    @FindBy(css = ".github-enterprise-choose-server-step .dropdown-server .Dropdown-button")
    WebElement dropdownServer;

    @FindBy(css = ".github-enterprise-choose-server-step .button-add-server")
    WebElement buttonServerAdd;

    @FindBy(css = ".github-enterprise-choose-server-step .button-next-step")
    WebElement buttonServerNext;

    @Override
    public void selectGithubCreation() {
        wait.until(ExpectedConditions.visibilityOf(githubEnterpriseCreationButton)).click();
        logger.info("Selected github enterprise");
    }

    public void clickAddServerButton() {
        wait.until(ExpectedConditions.visibilityOf(buttonServerAdd)).click();
    }

    public void clickChooseServerNextStep() {
        wait.until(ExpectedConditions.visibilityOf(buttonServerNext)).click();
    }

}

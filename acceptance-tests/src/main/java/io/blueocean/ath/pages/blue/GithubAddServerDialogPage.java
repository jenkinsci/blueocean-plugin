package io.blueocean.ath.pages.blue;

import io.blueocean.ath.CustomExpectedConditions;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebElementUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public class GithubAddServerDialogPage {
    private Logger logger = Logger.getLogger(GithubAddServerDialogPage.class);

    @Inject
    public GithubAddServerDialogPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    @Inject
    WaitUtil wait;

    @Inject
    WebDriver driver;

    @FindBy(css = ".github-enterprise-add-server-dialog .text-name input")
    WebElement textName;

    @FindBy(css = ".github-enterprise-add-server-dialog .text-url input")
    WebElement textUrl;

    @FindBy(css = ".github-enterprise-add-server-dialog .button-create-server")
    WebElement buttonCreate;

    @FindBy(css = ".github-enterprise-add-server-dialog .btn-secondary")
    WebElement buttonCancel;


    public void enterServerName(String name) {
        logger.info(String.format("enter server name '%s", name));
        WebElementUtils.setText(
            wait.until(ExpectedConditions.visibilityOf(textName)),
            name
        );
    }

    public void enterServerUrl(String url) {
        logger.info(String.format("enter server url '%s", url));
        WebElementUtils.setText(
            wait.until(ExpectedConditions.visibilityOf(textUrl)),
            url
        );
    }

    public void clickSaveServerButton() {
        logger.info("clicking save button");
        wait.until(ExpectedConditions.visibilityOf(buttonCreate)).click();
    }

    public void clickCancelButton() {
        wait.until(ExpectedConditions.visibilityOf(buttonCancel)).click();
    }


    public void findFormErrorMessage(String errorMessage) {
        wait.until(CustomExpectedConditions.textMatchesAnyElement(
            By.cssSelector(".github-enterprise-add-server-dialog .FormElement .ErrorMessage"),
            Pattern.compile(errorMessage)
        ));
        logger.info("Found error message = " + errorMessage);
    }

    public boolean hasFormErrorMessage(String errorMessage) {
        try {
            wait.until(CustomExpectedConditions.textMatchesAnyElement(
                By.cssSelector(".github-enterprise-add-server-dialog .FormElement .ErrorMessage"),
                Pattern.compile(errorMessage)
            ));
            logger.info("Found expected error message = " + errorMessage);
            return true;
        } catch (Exception  e) {
            return false;
        }
    }


    public void waitForErrorMessagesGone() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".github-enterprise-add-server-dialog .FormElement .ErrorMessage")));
    }

    public void wasDismissed() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".github-enterprise-add-server-dialog")), 120000);
    }
}

package io.blueocean.ath.pages.blue;

import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.CustomExpectedConditions;
import io.blueocean.ath.WaitUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public class GithubAddServerDialogPage implements WebDriverMixin {
    private Logger logger = LoggerFactory.getLogger(GithubAddServerDialogPage.class);

    @Inject
    WaitUtil wait;

    public void enterServerName(String name) {
        logger.info(String.format("enter server name '%s", name));
        find(".github-enterprise-add-server-dialog .text-name input").setText(name);
    }

    public void enterServerUrl(String url) {
        logger.info(String.format("enter server url '%s", url));
        find(".github-enterprise-add-server-dialog .text-url input").setText(url);
    }

    public void clickSaveServerButton() {
        logger.info("clicking save button");
        find(".github-enterprise-add-server-dialog .button-create-server").click();
    }

    public void clickCancelButton() {
        find(".github-enterprise-add-server-dialog .btn-secondary").click();
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

package io.blueocean.ath.pages.classic;

import io.blueocean.ath.BaseUrl;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.blueocean.ath.LocalDriverElement.find;
import static io.blueocean.ath.LocalDriverElement.go;

@Singleton
public class LoginPage{
    Logger logger = Logger.getLogger(LoginPage.class);
    @Inject
    WebDriver driver;

    @Inject @BaseUrl
    String base;

    @FindBy(id ="j_username")
    WebElement loginUsername;

    @FindBy(name = "j_password")
    WebElement loginPassword;

    @Inject
    public LoginPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public static String getUsername() {
        return System.getProperty("adminUsername", "alice");
    }

    public static String getPassword() {
        return System.getProperty("adminPassword", "alice");
    }

    public void open() {
        go(base + "/login");
        Assert.assertEquals(base + "/login", driver.getCurrentUrl());
    }

    public void login() {
        open();
        find("input[name=j_username]").sendKeys(getUsername());
        find("input[name=j_password]").sendKeys(getPassword());
        find("//button[contains(text(), 'log')]").click();
        find("//a[contains(@href, 'logout')]").isVisible();
        logger.info("Logged in as " + getUsername());
    }
}

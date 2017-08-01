package io.blueocean.ath.pages.classic;

import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        return "alice";
    }

    public static String getPassword() {
        return "alice";
    }

    @Inject
    WaitUtil wait;
    public void open() {
        driver.get(base + "/login");
        Assert.assertEquals(base + "/login", driver.getCurrentUrl());
    }

    public void login() {
        open();


        WebElement usernameField = wait.until(By.id("j_username"));
        usernameField.sendKeys(getUsername());

        wait.until(By.name("j_password")).sendKeys(getPassword());

        wait.until(By.xpath("//*/button[contains(text(), 'log')]")).click();
        wait.until(driver -> {
            if(driver.getCurrentUrl().contains("loginError")) {
                throw new RuntimeException("Error logging in");
            }
            return ExpectedConditions.urlToBe(base + "/").apply(driver);
        });
        logger.info("Logged in as alice");
    }
}

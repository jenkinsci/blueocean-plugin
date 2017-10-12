package io.blueocean.ath.pages.classic;

import io.blueocean.ath.BasePage;
import org.apache.log4j.Logger;
import org.junit.Assert;

import javax.inject.Singleton;

@Singleton
public class LoginPage extends BasePage {
    Logger logger = Logger.getLogger(LoginPage.class);

    public static String getUsername() {
        return System.getProperty("adminUsername", "alice");
    }

    public static String getPassword() {
        return System.getProperty("adminPassword", "alice");
    }

    public void open() {
        go("/login");
        Assert.assertEquals("/login", getRelativeUrl());
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

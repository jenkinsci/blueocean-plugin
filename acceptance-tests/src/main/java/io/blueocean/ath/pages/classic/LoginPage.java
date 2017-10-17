package io.blueocean.ath.pages.classic;

import io.blueocean.ath.BasePage;
import io.blueocean.ath.Config;
import org.apache.log4j.Logger;
import org.junit.Assert;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginPage extends BasePage {
    @Inject
    Config cfg;

    Logger logger = Logger.getLogger(LoginPage.class);

    public String getUsername() {
        return cfg.getString("adminUsername", "alice");
    }

    public String getPassword() {
        return cfg.getString("adminPassword", "alice");
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

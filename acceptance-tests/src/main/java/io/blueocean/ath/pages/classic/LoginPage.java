package io.blueocean.ath.pages.classic;

import io.blueocean.ath.BasePage;
import io.blueocean.ath.JenkinsUser;
import org.apache.log4j.Logger;
import org.junit.Assert;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginPage extends BasePage {
    @Inject
    JenkinsUser admin;

    Logger logger = Logger.getLogger(LoginPage.class);

    public void open() {
        go("/login");
        Assert.assertEquals("/login", getRelativeUrl());
    }

    public void login() {
        open();
        find("#j_username").sendKeys(admin.username);
        find("input[name=j_password]").sendKeys(admin.password);
        find("//button[contains(text(), 'log')]").click();
        find("//a[contains(@href, 'logout')]").isVisible();
        logger.info("Logged in as " + admin.username);
    }
}

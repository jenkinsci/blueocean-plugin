package io.blueocean.ath.pages.classic;

import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.JenkinsUser;
import org.junit.Assert;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginPage implements WebDriverMixin {
    @Inject
    JenkinsUser admin;

    Logger logger = LoggerFactory.getLogger(LoginPage.class);

    public void open() {
        go("/login");
        Assert.assertEquals("/login", getRelativeUrl());
    }

    public void login() {
        open();
        find("#j_username").sendKeys(admin.username);
        find("input[name=j_password]").sendKeys(admin.password);
        // JENKINS-50477 introduced a new login screen, which was released in core 2.128.
        // This allows us to log in with both login screens - starting with the new style.
        if (find("input[name=Submit]").isPresent()) {
            logger.info("Logging in via post-2.128 style log in page");
            find("input[name=Submit]").click();
        } else {
            logger.info("Logging in via pre-2.128 style log in page");
            find("//button[contains(text(), 'Sign')]").click();
        }

        find("//a[contains(@href, 'logout')]").isDisplayed();
        logger.info("Logged in as " + admin.username);
    }
}

package io.blueocean.ath.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public abstract class PageObject {
    final protected WebDriver driver;
    final protected String base;

    public PageObject(WebDriver driver, String base) {
        this.driver = driver;
        this.base = base;

        PageFactory.initElements(driver, this);
    }
}

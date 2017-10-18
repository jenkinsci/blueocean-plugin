package io.blueocean.ath;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Provides utility methods to downstream test classes
 */
public class BasePage {
    private static Pattern URL_WITH_PROTOCOL = Pattern.compile("^[a-zA-Z]+://.*");

    @Inject
    protected WebDriver driver;

    @Inject @BaseUrl
    protected String base;

    public BasePage() {
    }

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Gets the provided WebDriver instance
     * @return from driver
     */
    protected WebDriver getDriver() {
        return driver;
    }

    /**
     * Gets the current browser URL
     * @return current url
     */
    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    /**
     * Gets the browser url relative to the base
     * @return relative url
     */
    public String getRelativeUrl() {
        return getCurrentUrl().substring(base.length());
    }

    /**
     * Navigates to a relative url to the base url
     * @param url where to go
     */
    public void go(String url) {
        String addr = url;
        if (!URL_WITH_PROTOCOL.matcher(url).matches()) {
            addr = base + url;
        }
        getDriver().get(addr);
    }

    /**
     * Finds an element by the provided expression {@see SmartWebElement}
     * @param expr css or xpath; if it starts with a /, XPath is used
     * @return a new SmartWebElement
     */
    public SmartWebElement find(String expr) {
        return new SmartWebElement(getDriver(), expr);
    }

    /**
     * Utility to click based on provided expression
     * @param expr css or xpath; if it starts with a /, XPath is used
     */
    public void click(String expr) {
        find(expr).click();
    }

    /**
     * Executes javascript, returns the result
     * @param script javascript to execute
     * @return the result
     */
    public <T> T eval(String script, Object... env) {
        return (T)((JavascriptExecutor)getDriver()).executeScript(script, env);
    }
}

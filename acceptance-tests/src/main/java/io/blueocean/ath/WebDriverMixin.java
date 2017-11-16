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
public interface WebDriverMixin {
    class Const {
        private static Pattern URL_WITH_PROTOCOL = Pattern.compile("^[a-zA-Z]+://.*");
    }

    /**
     * Gets the provided WebDriver instance
     * @return from driver
     */
    default WebDriver getDriver() {
        return LocalDriver.getDriver();
    }

    /**
     * Gets the current browser URL
     * @return current url
     */
    default String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    /**
     * Gets the browser url relative to the base
     * @return relative url
     */
    default String getRelativeUrl() {
        return getCurrentUrl().substring(LocalDriver.getUrlBase().length());
    }

    /**
     * Navigates to a relative url to the base url
     * @param url where to go
     */
    default void go(String url) {
        String addr = url;
        if (!Const.URL_WITH_PROTOCOL.matcher(url).matches()) {
            addr = LocalDriver.getUrlBase() + url;
        }
        getDriver().get(addr);
    }

    /**
     * Navigate the back in browser history.
     */
    default void back() {
        getDriver().navigate().back();
    }

    /**
     * Navigate forward in browser history.
     */
    default void forward() {
        getDriver().navigate().forward();
    }

    /**
     * Navigate forward or back in browser history.
     * Negative input means back, positive is forward.
     * @param historyCount
     */
    default void go(int historyCount) {
        WebDriver.Navigation nav = getDriver().navigate();

        for (int i = 0; i < Math.abs(historyCount); i++) {
            if (historyCount > 0) {
                nav.forward();
            } else {
                nav.back();
            }
        }
    }

    /**
     * Finds an element by the provided expression {@see SmartWebElement}
     * @param expr css or xpath; if it starts with a /, XPath is used
     * @return a new SmartWebElement
     */
    default SmartWebElement find(String expr) {
        return new SmartWebElement(getDriver(), expr);
    }

    /**
     * Utility to click based on provided expression
     * @param expr css or xpath; if it starts with a /, XPath is used
     */
    default void click(String expr) {
        find(expr).click();
    }

    /**
     * Executes javascript, returns the result
     * @param script javascript to execute
     * @return the result
     */
    default <T> T eval(String script, Object... env) {
        return (T)((JavascriptExecutor)getDriver()).executeScript(script, env);
    }
}

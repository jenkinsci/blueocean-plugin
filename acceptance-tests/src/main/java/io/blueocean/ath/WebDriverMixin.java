package io.blueocean.ath;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * @param expr css or xpath; if it starts with a /, XPath is used
     * @return a {@link By} locator for the given expession
     */
    static By exprToBy(String expr) {
        By by;
        if (expr.startsWith("/")) {
            by = By.xpath(expr);
        } else {
            by = By.cssSelector(expr);
        }
        return by;
    }

    /**
     * Finds an element by the provided expression with an implicit wait for the element to be present.
     * If searching for an element to assert it's not present, use {@link #findNow}
     *
     * @param expr css or xpath; if it starts with a /, XPath is used
     * @return a new {@link SmartWebElement}
     */
    default SmartWebElement find(String expr) {
        return find(exprToBy(expr));
    }

    /**
     * Finds an element by the provided locator with an implicit wait for the element to be present.
     * If searching for an element to assert it's not present, use {@link #findNow}
     *
     * @param locator 'By' locator
     * @return a new {@link SmartWebElement}
     */
    default SmartWebElement find(By locator) {
        List<WebElement> elements = LocalDriver.buildWait()
            .until(ExpectedConditions.numberOfElementsToBe(locator, 1));
        return (SmartWebElement) LocalDriver.wrapElement(getDriver(), elements.get(0), locator);
    }

    /**
     * Finds elements by the provided expression with an implicit wait for the elements to be present.
     * If searching for elements to assert they're not present, use {@link #findNow}
     *
     * @param expr css or xpath; if it starts with a /, XPath is used
     * @return list of {@link SmartWebElement}
     */
    default List<WebElement> findMany(String expr) {
        return findMany(exprToBy(expr));
    }

    /**
     * Finds elements by the provided locator with an implicit wait for the elements to be present.
     * If searching for elements to assert they're not present, use {@link #findNow}
     *
     * @param locator 'By' locator
     * @return list of {@link SmartWebElement}
     */
    default List<WebElement> findMany(By locator) {
        List<WebElement> elements = LocalDriver.buildWait()
            .until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, 0));
        return LocalDriver.wrapElements(getDriver(), elements, locator);
    }

    default <T> T untilCondition(ExpectedCondition<T> condition) {
        // TODO: convert smart web element??
        return LocalDriver.buildWait().until(condition);
    }

    // TODO: implement until(Function ...)

    /**
     * Finds elements by the provided expression with no implicit waits.
     * Useful to search for an element and assert it doesn't exist.
     *
     * @param expr css or xpath; if it starts with a /, XPath is used
     * @return list of {@link WebElement}
     */
    default List<WebElement> findNow(String expr) {
        return findNow(exprToBy(expr));
    }

    /**
     * Finds elements by the provided locator with no implicit waits.
     * Useful to search for an element and assert it doesn't exist.
     *
     * @param locator 'By' locator
     * @return list of {@link WebElement}
     */
    default List<WebElement> findNow(By locator) {
        return LocalDriver.getDriver().findElements(locator);
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

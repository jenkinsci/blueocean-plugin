package io.blueocean.ath;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Provides access to the current WebDriver instance.
 * Contains some utility methods for working with waits and SmartWebElement.
 * TODO: rename this class as it's *not* a WebDriver but rather more of a DriverHolder.
 */
public class LocalDriver {
    private static ThreadLocal<WebDriver> CURRENT_WEB_DRIVER = new ThreadLocal<>();
    public static final int DEFAULT_TIMEOUT = Integer.getInteger("webDriverDefaultTimeout", 3000);

    public static void setCurrent(WebDriver driver) {
        CURRENT_WEB_DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        return CURRENT_WEB_DRIVER.get();
    }

    public static void destroy() {
        WebDriver driver = CURRENT_WEB_DRIVER.get();
        if (driver != null) {
            try {
                driver.close();
            } catch(Exception e) {
                // ignore, this happens when running individual tests sometimes
            }
        }
    }

    private static String urlBase;

    public static String getUrlBase() {
        return urlBase;
    }

    public static void setUrlBase(String base) {
        urlBase = base;
    }

    /**
     * @return a FluentWait with default polling / timeout / exception handling
     */
    static FluentWait<WebDriver> buildWait() {
        return new FluentWait<>(getDriver())
            .pollingEvery(100, TimeUnit.MILLISECONDS)
            .withTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
            .ignoring(NoSuchElementException.class);
    }

    /**
     * Wraps the supplied element in SmartWebElement if not already a SWE
     */
    static WebElement wrapElement(WebDriver driver, WebElement element, By locator) {
        if (element instanceof SmartWebElement) {
            return element;
        }
        return new SmartWebElement(driver, element, locator.toString());
    }

    /**
     * Wraps the supplied elements in SmartWebElement if not already a SWE
     */
    static List<WebElement> wrapElements(WebDriver driver, List<WebElement> elements, By locator) {
        return elements.stream()
            .map(element -> wrapElement(driver, element, locator) )
            .collect(Collectors.toList());
    }
}

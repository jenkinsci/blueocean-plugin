package io.blueocean.ath;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;

import java.util.List;
import java.util.Set;

/**
 * Wrapper around an underlying WebDriver that
 * consistently handles waits automatically.
 *
 * Accepts expressions for css and xpath, if the provided lookup starts with a /, XPath is used
 */
public class LocalDriver implements WebDriver {
    private static ThreadLocal<WebDriver> CURRENT_WEB_DRIVER = new ThreadLocal<>();
    private static boolean sauceLabsMode;

    public static void setCurrent(WebDriver driver) {
        CURRENT_WEB_DRIVER.set(driver);
        sauceLabsMode = false;
    }

    public static WebDriver getDriver() {
        return CURRENT_WEB_DRIVER.get();
    }

    public static void executeScript(String str) {
        WebDriver driver = CURRENT_WEB_DRIVER.get();
        if (driver != null) {
            ((JavascriptExecutor) driver).executeScript(str);
        }
    }

    public static void annotate(String text) {
        LocalDriver.executeSauce("context=" + text);
    }

    public static void executeSauce(String s) {
        WebDriver driver = CURRENT_WEB_DRIVER.get();
        if (driver != null && sauceLabsMode) {
            LocalDriver.executeScript("sauce:" + s);
        }
    }

    public static void destroy() {
        WebDriver driver = CURRENT_WEB_DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
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

    public static void enableSauce() {
        sauceLabsMode = true;
    }

    public static boolean isSauceLabsMode() {
        return sauceLabsMode;
    }

    /**
     * Used for callbacks in a specific browser context
     */
    public interface Procedure {
        void execute() throws Exception;
    }

    @Override
    public void get(String s) {
        getDriver().get(s);
    }

    @Override
    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return getDriver().getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return new SmartWebElement(getDriver(), by).getElements();
    }

    @Override
    public WebElement findElement(By by) {
        return new SmartWebElement(getDriver(), by).getElement();
    }

    @Override
    public String getPageSource() {
        return getDriver().getPageSource();
    }

    @Override
    public void close() {
        WebDriver driver = getDriver();
        if (driver != null) {
            try {
                driver.close();
            } catch(Exception e) {
                // ignore, this happens when running individual tests sometimes
            }
        }
    }

    @Override
    public void quit() {
        getDriver().quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return getDriver().getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return getDriver().getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return getDriver().switchTo();
    }

    @Override
    public Navigation navigate() {
        return getDriver().navigate();
    }

    @Override
    public Options manage() {
        return getDriver().manage();
    }

    /**
     * Push a specific driver into context and execute the proc
     * @param driver new driver in context
     * @param proc procedure to execute
     */
    public static void use(WebDriver driver, Procedure proc) {
        WebDriver previous = CURRENT_WEB_DRIVER.get();
        try {
            CURRENT_WEB_DRIVER.set(driver);
            try {
                proc.execute();
            } catch(RuntimeException e) {
                throw e;
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            if (previous == null) {
                CURRENT_WEB_DRIVER.remove();
            } else {
                CURRENT_WEB_DRIVER.set(previous);
            }
        }
    }
}

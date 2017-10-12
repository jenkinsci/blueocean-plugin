package io.blueocean.ath;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around an underlying WebDriver that
 * consistently handles waits automatically.
 *
 * Accepts expressions for css and xpath, if the provided lookup starts with a /, XPath is used
 */
public class LocalDriverElement implements WebElement {
    static ThreadLocal<WebDriver> CURRENT_WEB_DRIVER = new ThreadLocal<>();

    protected String expr;

    public LocalDriverElement(String expr) {
        this.expr = expr;
    }

    /**
     * Finds an element by the provided expression {@see LocalDriverElement}
     * @param expr css or xpath; if it starts with a /, XPath is used
     * @return a new LocalDriverElement
     */
    public static LocalDriverElement find(String expr) {
        return new LocalDriverElement(expr);
    }

    /**
     * Executes javascript, returns the result
     * @param script javascript to execute
     * @return the result
     */
    public static Object eval(String script) {
        return new FluentWait<>(CURRENT_WEB_DRIVER.get())
            .pollingEvery(100, TimeUnit.MILLISECONDS)
            .withTimeout(WaitUtil.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
            .ignoring(NoSuchElementException.class)
            .until(ExpectedConditions.jsReturnsValue(script));
    }

    /**
     * Navigates to a specified url
     * @param url where to go
     */
    public static void go(String url) {
        CURRENT_WEB_DRIVER.get().get(url);
    }

    /**
     * Gets a WebDriver instance
     * @return from threadlocal
     */
    protected WebDriver getDriver() {
        return CURRENT_WEB_DRIVER.get();
    }

    /**
     * Gets elements
     * @return the elements found
     */
    public List<WebElement> getElements() {
        By by;
        if (expr.startsWith("/")) {
            by = By.xpath(expr);
        } else {
            by = By.cssSelector(expr);
        }
        return new FluentWait<>(getDriver())
            .pollingEvery(100, TimeUnit.MILLISECONDS)
            .withTimeout(WaitUtil.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
            .ignoring(NoSuchElementException.class)
            //.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
            .until(ExpectedConditions.numberOfElementsToBeMoreThan(by, 0));
    }

    /**
     * Gets the first matching element
     * @return see description
     */
    public WebElement getElement() {
        return getElements().iterator().next();
    }

    /**
     * Iterates over all found elements with the given function
     * @param fn to perform on the elements
     */
    public void forEach(java.util.function.Consumer<WebElement> fn) {
        for (WebElement e : getElements()) {
            fn.accept(e);
        }
    }

    public boolean isVisible() {
        return getElement().isDisplayed();
    }

    @Override
    public void click() {
        forEach(e -> e.click());
    }

    @Override
    public void submit() {
        forEach(e -> e.submit());
    }

    @Override
    public void sendKeys(CharSequence... charSequences) {
        forEach(e -> e.sendKeys(charSequences));
    }

    @Override
    public void clear() {
        forEach(e -> e.clear());
    }

    @Override
    public String getTagName() {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.getTagName();
    }

    @Override
    public String getAttribute(String s) {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.getAttribute(s);
    }

    @Override
    public boolean isSelected() {
        WebElement el = getElement();
        if (el == null) {
            return false;
        }
        return el.isSelected();
    }

    @Override
    public boolean isEnabled() {
        WebElement el = getElement();
        if (el == null) {
            return false;
        }
        return el.isEnabled();
    }

    @Override
    public String getText() {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.getText();
    }

    @Override
    public List<WebElement> findElements(By by) {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.findElement(by);
    }

    @Override
    public boolean isDisplayed() {
        WebElement el = getElement();
        if (el == null) {
            return false;
        }
        return el.isDisplayed();
    }

    @Override
    public Point getLocation() {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.getLocation();
    }

    @Override
    public Dimension getSize() {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.getSize();
    }

    @Override
    public Rectangle getRect() {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.getRect();
    }

    @Override
    public String getCssValue(String s) {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.getCssValue(s);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        WebElement el = getElement();
        if (el == null) {
            return null;
        }
        return el.getScreenshotAs(outputType);
    }
}

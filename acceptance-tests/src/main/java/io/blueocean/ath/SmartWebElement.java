package io.blueocean.ath;

import com.google.common.base.Preconditions;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Wrapper around an underlying WebDriver that automatically handles waits and common gotchas
 * within blueocean.
 *
 * Accepts expressions for css and xpath, if the provided lookup starts with a /, XPath is used
 */
public class SmartWebElement implements WebElement {
    private static Logger logger = LoggerFactory.getLogger(SmartWebElement.class);
    public static final int DEFAULT_TIMEOUT = Integer.getInteger("webDriverDefaultTimeout", 3000);
    public static final int RETRY_COUNT = 3;

    private WebDriver driver;
    protected String expr;
    protected By by;

    public SmartWebElement(WebDriver driver, String expr) {
        this(driver, expr, exprToBy(expr));
    }

    public SmartWebElement(WebDriver driver, By by) {
        this(driver, by.toString(), by);
    }

    public SmartWebElement(WebDriver driver, String expr, By by) {
        this.driver = driver;
        this.expr = expr;
        this.by = by;
    }

    private static By exprToBy(String expr) {
        By by;
        if (expr.startsWith("/")) {
            by = By.xpath(expr);
        } else {
            by = By.cssSelector(expr);
        }
        return by;
    }

    /**
     * Gets a WebDriver instance
     * @return from threadlocal
     */
    protected WebDriver getDriver() {
        return driver;
    }

    /**
     * Gets elements
     * @return the elements found
     */
    public List<WebElement> getElements() {
        return new FluentWait<>(getDriver())
            .pollingEvery(100, TimeUnit.MILLISECONDS)
            .withTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
            .ignoring(NoSuchElementException.class)
            .until(driver -> driver.findElements(SmartWebElement.this.by));
    }

    /**
     * Gets the first matching element
     * @return see description
     */
    public WebElement getElement() throws NoSuchElementException {
        List<WebElement> elements = getElements();
        if (elements == null || elements.isEmpty()) {
            throw new NoSuchElementException("Nothing matched for: " + expr);
        }
        if (elements.size() > 1) {
            throw new NoSuchElementException("Too many elements returned for: " + expr);
        }
        return elements.get(0);
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

    /**
     * Determines if the element is visible
     * @return true if visible, false if not
     */
    public boolean isVisible() {
        WebDriverWait wait = new WebDriverWait(getDriver(), 60);
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        return getDriver().findElement(by).isDisplayed();
    }


    /**
     * Determines if the element is present
     * @return true if present, false if not
     */
    public boolean isPresent() {
        try {
            if (getDriver().findElement(by) == null) {
                return false;
            }
            return true;
        } catch(NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public void click() {
        WebDriverWait wait = new WebDriverWait(getDriver(), 60);
        wait.until(ExpectedConditions.elementToBeClickable(by));
        getDriver().findElement(by).click();
    }

    @Override
    public void submit() {
        WebElement e = getElement();
        e.submit();
    }

    @Override
    public void sendKeys(CharSequence... charSequences) {
        WebElement e = getElement();
        e.sendKeys(charSequences);
    }

    /**
     * Executes a script with 'el' bound to the first element and 'elements'
     * to all found elements, returns the result
     * @param script js to execute
     */
    public <T> T eval(String script) {
        String js = "return (function(el,elements){" + script + "})(arguments[0],arguments[1])";
        List<WebElement> elements = getElements();
        WebElement el = elements.iterator().next();
        return (T)((JavascriptExecutor)getDriver()).executeScript(script, el, elements);
    }

    /**
     * Send an event to the matched elements - e.g. 'blur' or 'change'
     * @param type
     */
    public void sendEvent(String type) {
        WebElement e = getElement();
        sendEvent(e, type);
    }

    protected void sendEvent(WebElement el, String type) {
        StringBuilder script = new StringBuilder(
            "return (function(a,b,c,d){" +
                "c=document," +
                "c.createEvent" +
                "?(d=c.createEvent('HTMLEvents'),d.initEvent(b,!0,!0),a.dispatchEvent(d))" +
                ":(d=c.createEventObject(),a.fireEvent('on'+b,d))})"
        );
        script.append("(arguments[0],'").append(type.replace("'", "\\'")).append("');");
        ((JavascriptExecutor)getDriver()).executeScript(script.toString(), el);
    }

    protected void sendInputEvent(WebElement el) {
        sendEvent(el, "input");
    }

    /**
     * Asserts the element is an input or textarea
     * @param element
     */
    private static void validateTextElement(WebElement element) {
        String tagName = element.getTagName().toLowerCase();
        Preconditions.checkArgument(
            "input".equals(tagName) || "textarea".equals(tagName),
            "element must should be input or textarea but was %s",
            tagName
        );
    }

    /**
     * Sets the matched input to the given text, if setting to empty string
     * there is some special handling to clear the input such that events are
     * properly handled across platforms by sending an additional 'oninput' event
     * @param text text to use
     */
    public void setText(CharSequence... text) {
        WebElement e = getElement();
        validateTextElement(e);
        e.clear();
        e.sendKeys(text);
        // If setting the text empty, also send input event
        if (text.length == 1 && "".equals(text[0])) {
            // b'cuz React, see: https://github.com/facebook/react/issues/8004
            sendInputEvent(e);
        }
    }

    @Override
    public void clear() {
        WebElement e = getElement();
        e.clear();
        // b'cuz React, see: https://github.com/facebook/react/issues/8004
        sendInputEvent(e);
    }

    @Override
    public String getTagName() {
        WebElement e = getElement();
        return e.getTagName();
    }

    @Override
    public String getAttribute(String s) {
        WebElement e = getElement();
        return e.getAttribute(s);
    }

    @Override
    public boolean isSelected() {
        WebElement e = getElement();
        return e.isSelected();
    }

    @Override
    public boolean isEnabled() {
        WebElement e = getElement();
        return e.isEnabled();
    }

    @Override
    public String getText() {
        WebElement e = getElement();
        return e.getText();
    }

    @Override
    public List<WebElement> findElements(By by) {
        WebElement e = getElement();
        return e.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        WebElement e = getElement();
        return e.findElement(by);
    }

    @Override
    public boolean isDisplayed() {
        WebElement e = getElement();
        return e.isDisplayed();
    }

    @Override
    public Point getLocation() {
        WebElement e = getElement();
        return e.getLocation();
    }

    @Override
    public Dimension getSize() {
        WebElement e = getElement();
        return e.getSize();
    }

    @Override
    public Rectangle getRect() {
        WebElement e = getElement();
        return e.getRect();
    }

    @Override
    public String getCssValue(String s) {
        WebElement e = getElement();
        return e.getCssValue(s);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
        WebElement e = getElement();
        return e.getScreenshotAs(outputType);
    }
}

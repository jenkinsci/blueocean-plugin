package io.blueocean.ath;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.List;

/**
 * Wrapper around an underlying WebDriver that automatically handles waits and common gotchas.
 */
public class SmartWebElement implements WebElement {
    private static Logger logger = Logger.getLogger(SmartWebElement.class);
    public static final int RETRY_COUNT = 3;

    private WebDriver driver;
    protected WebElement element;
    protected String expr;

    SmartWebElement(WebDriver driver, WebElement element, String expr) {
        this.driver = driver;
        this.element = element;
        this.expr = StringUtils.defaultIfBlank(expr, "<not set>");
    }

    private WebElement getElement() {
        return element;
    }

    /**
     * Gets a WebDriver instance
     * @return from threadlocal
     */
    protected WebDriver getDriver() {
        return driver;
    }

    /**
     * @return a FluentWait with default polling / timeout / exception handling
     */
    private FluentWait<WebDriver> buildWait() {
        return LocalDriver.buildWait();
    }

    @Override
    public void click() {
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                WebElement e = buildWait().until(ExpectedConditions.elementToBeClickable(element));
                e.click();
                if (i > 0) {
                    logger.info(String.format("retry click successful for %s", expr));
                }
                return;
            } catch (WebDriverException ex) {
                if (ex.getMessage().contains("is not clickable at point")) {
                    logger.warn(String.format("%s not clickable: will retry click", expr));
                    logger.debug("exception: " + ex.getMessage());
                    try {
                        // typically this is during an animation, which should not take long
                        Thread.sleep(500);
                    } catch(InterruptedException ie) {
                        // ignore
                    }
                } else {
                    throw ex;
                }
            }
        }
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
        // TODO: validate this change is correct
        WebElement el = getElement();
        return (T)((JavascriptExecutor)getDriver()).executeScript(script, el);
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
    public List<WebElement> findElements(By locator) {
        return buildWait().until((WebDriver driver) -> {
            List<WebElement> elements = getElement().findElements(locator);
            return LocalDriver.wrapElements(driver, elements, locator);
        });
    }

    @Override
    public WebElement findElement(By locator) {
        return buildWait().until((WebDriver driver) -> {
            WebElement matchedElement = getElement().findElement(locator);
            return LocalDriver.wrapElement(getDriver(), matchedElement, locator);
        });
    }

    @Override
    public boolean isDisplayed() {
        WebElement e = buildWait().until(ExpectedConditions.visibilityOf(element));
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

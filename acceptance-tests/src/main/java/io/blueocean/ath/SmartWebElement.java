package io.blueocean.ath;

import com.google.common.base.Preconditions;
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
public class SmartWebElement implements WebElement {
    private WebDriver driver;
    protected String expr;
    protected By by;

    public SmartWebElement(WebDriver driver, String expr) {
        this.driver = driver;
        this.expr = expr;
        if (expr.startsWith("/")) {
            by = By.xpath(expr);
        } else {
            by = By.cssSelector(expr);
        }
    }

    public SmartWebElement(WebDriver driver, By by) {
        this.driver = driver;
        this.expr = by.toString();
        this.by = by;
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
            .withTimeout(WaitUtil.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
            .ignoring(NoSuchElementException.class)
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
        forEach(e -> sendEvent(e, type));
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

    private static void validateTextElement(WebElement element) {
        String tagName = element.getTagName().toLowerCase();
        Preconditions.checkArgument(
            "input".equals(tagName) || "textarea".equals(tagName),
            "element must should be input or textarea but was %s",
            tagName
        );
    }

    /**
     * Sets the matched inputs to the given text, if setting to empty string
     * there is some special handling to clear the input such that events are
     * properly handled across platforms by sending an additional 'oninput' event
     * @param text text to use
     */
    public void setText(CharSequence... text) {
        forEach(e -> {
            validateTextElement(e);
            e.clear();
            e.sendKeys(text);
            // If setting the text empty,
            if (text.length == 1 && "".equals(text[0])) {
                // b'cuz React, see: https://github.com/facebook/react/issues/8004
                sendInputEvent(e);
            }
        });
    }

    @Override
    public void clear() {
        forEach(e -> {
            e.clear();
            // b'cuz React, see: https://github.com/facebook/react/issues/8004
            sendInputEvent(e);
        });
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

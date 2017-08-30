package io.blueocean.ath;


import com.google.common.base.Preconditions;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class WebElementUtils {

    /**
     * Clears the value in a text element by selecting all text and backspacing it.
     *
     * @param element
     */
    public static void clearText(WebElement element) {
        checkTextElement(element);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.BACK_SPACE);
    }

    /**
     * Set the text in an element to exactly the value specified.
     * Selects all text and then sets the new value in its place.
     *
     * @param element
     * @param text
     */
    public static void setText(WebElement element, String text) {
        checkTextElement(element);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(text);
    }

    private static void checkTextElement(WebElement element) {
        String tagName = element.getTagName().toLowerCase();

        Preconditions.checkArgument(
            "input".equals(tagName) || "textarea".equals(tagName),
            "element must should be input or textarea but was %s",
            tagName
        );
    }

}

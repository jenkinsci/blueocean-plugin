package io.blueocean.ath;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;
import java.util.regex.Pattern;

public class CustomExpectedConditions {

    /**
     * An expectation for checking that any WebElement matching the given locator has text with a value as a part of it.
     * Similar to Selenium's ExpectedConditions.textMatches but supports a locator that returns multiple elements.
     *
     * @param locator
     * @param pattern
     * @return
     */
    public static ExpectedCondition<Boolean> textMatchesAnyElement(final By locator, final Pattern pattern) {
        return new ExpectedCondition<Boolean>() {
            private String currentValue = null;

            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    List<WebElement> elements = driver.findElements(locator);
                    for (WebElement elem : elements) {
                        currentValue = elem.getText();
                        if (pattern.matcher(currentValue).find()) {
                            return true;
                        }
                    }

                    return false;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public String toString() {
                return String
                    .format("text to match pattern \"%s\". Current text: \"%s\"", pattern.pattern(),
                        currentValue);
            }
        };
    }
}

package io.blueocean.ath;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;

/**
 * Utilities for working with Selenium 'By' (aka locators).
 *
 * @author cliffmeyers
 */
public abstract class Locate {

    /**
     * Transforms a WebElement instance to a 'By' locator that will return the element.
     * Convenient when you have a WebElement instance but need to use an API that uses 'By' locator.
     *
     * @param elem
     * @return element wrapped in 'By' locator
     */
    public static By byElem(WebElement elem) {
        return new ByElement(elem);
    }

    /**
     * An implementation of 'By' that wraps a single element.
     * Convenient when you have a WebElement instance but need to use an API that uses 'By' locator.
     */
    public static class ByElement extends By {

        private final WebElement elem;

        private ByElement(WebElement elem) {
            this.elem = elem;
        }

        @Override
        public List<WebElement> findElements(SearchContext context) {
            return Collections.singletonList(elem);
        }
    }
}

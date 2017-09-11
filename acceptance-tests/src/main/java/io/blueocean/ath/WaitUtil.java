package io.blueocean.ath;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Singleton
public class WaitUtil {
    private Logger logger = Logger.getLogger(WaitUtil.class);


    private WebDriver driver;

    @Inject
    public WaitUtil(WebDriver driver) {
        this.driver = driver;
    }

    public <T> T until(Function<WebDriver, T> function, long timeoutInMS, String errorMessage) {
        try {
            return new FluentWait<WebDriver>(driver)
                .pollingEvery(100, TimeUnit.MILLISECONDS)
                .withTimeout(timeoutInMS, TimeUnit.MILLISECONDS)
                .ignoring(NoSuchElementException.class)
                .until((WebDriver driver) -> function.apply(driver));
        } catch(Throwable t) {
            throw new AcceptanceTestException(errorMessage, t);
        }
    }

    public <T> T until(Function<WebDriver, T> function, long timeoutInMS) {
        return until(function, timeoutInMS, "Error while waiting for something");
    }
    public <T> T until(Function<WebDriver, T> function) {
        return until(function, 20000);
    }

    public <T> T until(Function<WebDriver, T> function, String errorMessage) {
        return until(function, 20000);
    }

    public WebElement until(WebElement element) {
        return until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement until(By by) {
        return until(ExpectedConditions.visibilityOfElementLocated(by), "Could not find " + by.toString());
    }


    public WebElement until(WebElement element, long timeoutInMS) {
        return until(ExpectedConditions.visibilityOf(element), timeoutInMS);
    }

    public WebElement until(By by, long timeoutInMS) {
        return until(ExpectedConditions.visibilityOfElementLocated(by), timeoutInMS);
    }
    public <T> Function<WebDriver, Integer> orVisible(Function<WebDriver, WebElement> trueCase, Function<WebDriver, WebElement> falseCase) {
        return driver -> {
            try {
                if(trueCase.apply(driver).isDisplayed()) {
                    return 1;
                }
            } catch (NotFoundException e) {
                if(falseCase.apply(driver).isDisplayed()) {
                    return 2;
                }
            }

            throw new NotFoundException();
        };
    }

}

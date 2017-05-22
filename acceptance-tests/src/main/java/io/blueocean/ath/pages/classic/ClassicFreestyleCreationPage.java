package io.blueocean.ath.pages.classic;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.api.classic.ClassicJobApi;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class ClassicFreestyleCreationPage {
    @Inject
    WebDriver driver;

    @Inject @BaseUrl
    String base;

    @Inject
    ClassicJobApi jobApi;

    @FindBy(id = "name")
    WebElement nameInput;

    @Inject
    public ClassicFreestyleCreationPage(WebDriver driver) {
         PageFactory.initElements(driver, this);
    }

    public void createJob(String jobName) throws UnirestException, IOException {
        driver.get(base + "/view/All/newJob");
        jobApi.deleteJob(jobName);
        nameInput.sendKeys(jobName);
    }
}


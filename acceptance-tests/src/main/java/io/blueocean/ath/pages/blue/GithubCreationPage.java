package io.blueocean.ath.pages.blue;

import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.LocalDriver;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.api.classic.ClassicJobApi;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.regex.Pattern;

@Singleton
public class GithubCreationPage implements WebDriverMixin {
    private Logger logger = Logger.getLogger(GithubCreationPage.class);

    @Inject
    public GithubCreationPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    @Inject
    @BaseUrl
    String baseUrl;

    @Inject
    WaitUtil wait;

    @Inject
    WebDriver driver;

    @Inject
    DashboardPage dashboardPage;

    @Inject
    ClassicJobApi jobApi;

    /**
     * Navigate to the creation page via dashboard
     */
    public void navigateToCreation() {
        dashboardPage.clickNewPipelineBtn();
    }

    public void selectGithubCreation() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.github-creation"))).click();
        logger.info("Selected github");
    }

    /**
     * Enter the specified token and press button to validate.
     * @param token
     */
    public void validateGithubOauthToken(String token) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".text-token input")), 1000);
        element.sendKeys(token);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".button-connect")), 1000).click();
        logger.info("Set Oauth token");
    }

    public void findFormErrorMessage(String errorMessage) {
        wait.until(ExpectedConditions.textMatches(
            By.cssSelector(".FormElement .ErrorMessage"),
            Pattern.compile(errorMessage)
        ));
        logger.info("Found error message = " + errorMessage);
    }

    public void selectOrganization(String org) {
        By xpath = getOrgSelector(org);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(xpath));
        element.click();
    }
    public By getOrgSelector(String user) {
        return By.xpath("//div[@class='org-list-item']/span[text()='"+ user +"']");
    }
    public void selectPipelineToCreate(final String pipeline){
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".repo-list input")))
            .sendKeys(pipeline);

        // because the auto complete is added and removed quickly, sometimes the element becomes stale before we can click
        // so do a retry
        wait.click(By.xpath("//div[contains(@class, 'repo-list')]//div[contains(@class,'List-Item')]//span[text()='" + pipeline + "']"));
        logger.info("Selected pipeline to create");
    }

    public void clickCreatePipelineButton() {
        wait.retryAction("click create pipeline button", 3, driver -> {
            wait.click(By.cssSelector(".button-create"));
            return true;
        });
    }

    public void createPipeline(String apikey, String org, String pipeline) throws IOException {
        createPipeline(apikey, org, pipeline, false);
    }
    public void createPipeline(String apiKey, String org, String pipeline, boolean createJenkinsFile) throws IOException {
        beginCreationFlow(pipeline);
        completeCreationFlow(apiKey, org, pipeline, createJenkinsFile);
    }

    /**
     * @param jobName name of job to be created
     * @throws IOException
     */
    public void beginCreationFlow(String jobName) throws IOException {
        jobApi.deletePipeline(jobName);
        navigateToCreation();
        selectGithubCreation();
    }

    public void completeCreationFlow(String apiKey, String org, String pipeline, boolean createJenkinsFile) {
        if(wait.until(
                wait.orVisible(
                    driver -> driver.findElement(By.cssSelector(".text-token input")),
                    driver -> driver.findElement(getOrgSelector(org))
                )
        ) == 1) {
            validateGithubOauthToken(apiKey);
        }
        selectOrganization(org);

        logger.info("Select a repo to create");

        selectPipelineToCreate(pipeline);
        clickCreatePipelineButton();

        if(createJenkinsFile) {
            wait.until(ExpectedConditions.urlContains("pipeline-editor"), 30000);
            logger.info("AbstractPipeline created - now editing");
        } else {
            try {
                wait.until(ExpectedConditions.urlMatches(".*activity$"), 90000);
            } catch (Throwable e) {
                driver.get(baseUrl + "/blue/organizations/jenkins/" + pipeline + "/activity");
                wait.until(ExpectedConditions.urlMatches(".*activity"), 90000);
            }
            logger.info("AbstractPipeline created");
        }
    }

}

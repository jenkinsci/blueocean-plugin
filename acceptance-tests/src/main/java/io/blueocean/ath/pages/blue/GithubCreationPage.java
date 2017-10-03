package io.blueocean.ath.pages.blue;

import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.api.classic.ClassicJobApi;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.regex.Pattern;

@Singleton
public class GithubCreationPage {
    private Logger logger = Logger.getLogger(GithubCreationPage.class);

    @Inject
    public GithubCreationPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = "button.github-creation")
    public WebElement githubCreationBtn;

    @FindBy(css = ".text-token input")
    public WebElement apiKeyInput;

    @FindBy(css = ".button-connect")
    public WebElement connectButton;

    @FindBy(css = ".button-single-repo")
    public WebElement singlePipelineBtn;

    @FindBy(css = ".repo-list input")
    public WebElement pipelineSearchInput;

    @FindBy(css = ".button-create")
    public WebElement createBtn;

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
        dashboardPage.open();
        wait.until(ExpectedConditions.visibilityOf(dashboardPage.newPipelineButton))
            .click();;
        logger.info("Clicked on new pipeline button");
    }

    public void selectGithubCreation() {
        wait.until(ExpectedConditions.visibilityOf(githubCreationBtn)).click();
        logger.info("Selected github");
    }

    /**
     * Enter the specified token and press button to validate.
     * @param token
     */
    public void validateGithubOauthToken(String token) {
        WebElement element = wait.until(ExpectedConditions.visibilityOf((apiKeyInput)), 1000);
        element.sendKeys(token);
        connectButton.click();
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
    public void selectPipelineToCreate(String pipeline){
        wait.until(ExpectedConditions.visibilityOf(pipelineSearchInput))
            .sendKeys(pipeline);

        By xpath = By.xpath("//div[contains(@class, 'repo-list')]//div[contains(@class,'List-Item')]//span[text()='"+pipeline+"']");
        wait.until(ExpectedConditions.visibilityOfElementLocated(xpath)).click();
        logger.info("Selected pipeline to create");
    }

    public By emptyRepositoryCreateButton = By.cssSelector(".jenkins-pipeline-create-missing-jenkinsfile > div > button");

    public void createPipeline(String apikey, String org, String pipeline) throws IOException {
        createPipeline(apikey, org, pipeline, false);
    }
    public void createPipeline(String apiKey, String org, String pipeline, boolean createJenkinsFile) throws IOException {
        beginCreationFlow(org);
        completeCreationFlow(apiKey, org, pipeline, createJenkinsFile);
    }

    public void beginCreationFlow(String org) throws IOException {
        jobApi.deletePipeline(org);
        navigateToCreation();
        selectGithubCreation();
    }

    public void completeCreationFlow(String apiKey, String org, String pipeline, boolean createJenkinsFile) {
        if(wait.until(wait.orVisible(
            driver -> apiKeyInput,
            driver -> driver.findElement(getOrgSelector(org)))) == 1) {

            validateGithubOauthToken(apiKey);
        }
        selectOrganization(org);

        logger.info("Select a repo to create");

        selectPipelineToCreate(pipeline);

        wait.until(createBtn).click();

        if(createJenkinsFile) {
            WebElement createJenkinsFileButton = wait
                .until(ExpectedConditions.visibilityOfElementLocated(emptyRepositoryCreateButton));
            createJenkinsFileButton.click();
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

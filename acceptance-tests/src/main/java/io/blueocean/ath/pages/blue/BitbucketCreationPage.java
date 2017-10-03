package io.blueocean.ath.pages.blue;

import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.api.classic.ClassicJobApi;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
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
public class BitbucketCreationPage {
    private Logger logger = Logger.getLogger(BitbucketCreationPage.class);

    @Inject
    public BitbucketCreationPage(WebDriver driver) {
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

    public void selectBitbucketServerCreation() {
        wait.until(By.xpath("//span[text()='Bitbucket Server']")).click();
        logger.info("Selected bitbucket server");
    }

    public void clickAddServer() {
        wait.until(By.cssSelector(".button-add-server")).click();
        logger.info("Clicked addserver");
    }

    public void enterServerDetials(String serverName, String serverUrl) {
        wait.until(By.xpath("//input[@placeholder='My Bitbucket Server']")).sendKeys(serverName);
        wait.until(By.xpath("//input[@placeholder='https://mybitbucket.example.com']")).sendKeys(serverUrl);
        wait.until(By.cssSelector(".button-create-server")).click();
        wait.until(By.cssSelector(".button-next-step")).click();
        logger.info("Entered server details");
    }

    public void enterServerCredentials(String user, String pass) {
        wait.until(By.xpath("//input[ @class='TextInput-control'])[1]")).sendKeys(user);
        wait.until(By.xpath("//input[ @class='TextInput-control'])[2]")).sendKeys(pass);
        wait.until(By.cssSelector(".button-create-credental")).click();
        logger.info("Entered credentials");
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
        selectBitbucketServerCreation();
    }

    public void completeCreationFlow(String apiKey, String org, String pipeline, boolean createJenkinsFile) {

    }

}

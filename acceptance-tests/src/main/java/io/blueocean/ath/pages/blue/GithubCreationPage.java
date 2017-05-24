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

@Singleton
public class GithubCreationPage {
    private Logger logger = Logger.getLogger(GithubCreationPage.class);

    @Inject
    public GithubCreationPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//span[text()='Github']")
    public WebElement githubCreationBtn;

    @FindBy(css = "input[placeholder='Your Github access token']")
    public WebElement apiKeyInput;

    @FindBy(css = "button.Button.button-connect")
    public WebElement connectButton;

    @FindBy(xpath = "//p[text()='Create a Pipeline from a single repository.']")
    public WebElement singlePipelineBtn;

    @FindBy(css = "input[placeholder='Search...']")
    public WebElement pipelineSearchInput;

    @FindBy(css = "button.button-create")
    public WebElement createBtn;

    @Inject
    WaitUtil wait;

    @Inject
    WebDriver driver;

    @Inject
    DashboardPage dashboardPage;

    @Inject
    ClassicJobApi jobApi;

    public void setGithubOauthToken(String token) {
        WebElement element = wait.until(ExpectedConditions.visibilityOf((apiKeyInput)), 1000);
        element.sendKeys(token);
        connectButton.click();
        logger.info("Set Oauth token");
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
        wait.until(ExpectedConditions.visibilityOf(pipelineSearchInput));
        pipelineSearchInput.sendKeys(pipeline);

        By xpath = By.xpath("//div[contains(@class, 'repo-list')]//div[contains(@class,'List-Item')]//span[text()='"+pipeline+"']");
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(xpath));

        element.click();
        logger.info("Selected pipeline to create");
    }


    public void createPipeline(String apikey, String org, String pipeline) throws IOException {
        jobApi.deletePipeline(org);

        dashboardPage.open();
        dashboardPage.newPipelineButton.click();
        logger.info("Clicked on new pipeline button");
        githubCreationBtn.click();
        logger.info("Selected github");

        if(wait.until(wait.orVisisble(
            driver -> apiKeyInput,
            driver -> driver.findElement(getOrgSelector(org)))) == 1) {

            setGithubOauthToken(apikey);
        }
        selectOrganization(org);

        wait.until(ExpectedConditions.visibilityOf(singlePipelineBtn));
        singlePipelineBtn.click();

        logger.info("Select a single pipeline to create");

        selectPipelineToCreate(pipeline);

        createBtn.click();
        wait.until(ExpectedConditions.urlMatches(".*activity$"), 30000);
        logger.info("Pipeline created");
    }
}

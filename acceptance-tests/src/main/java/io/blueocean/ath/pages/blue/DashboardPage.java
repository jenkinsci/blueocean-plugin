package io.blueocean.ath.pages.blue;

import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.Pipeline;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DashboardPage {
    private Logger logger = Logger.getLogger(DashboardPage.class);

    @Inject
    @BaseUrl
    String base;

    @Inject
    public DashboardPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".btn-new-pipeline")
    public WebElement newPipelineButton;

    @Inject
    public WaitUtil wait;

    @Inject
    public WebDriver driver;

    public void open() {
        driver.get(base + "/blue/");
        logger.info("Navigated to dashboard page");
    }

    public By getSelectorForJob(String job) {
        return By.xpath("//*[@data-pipeline=\"" + job + "\"]");
    }

    public By getSelectorForAllJobRows() {
        return By.xpath("//*[contains(@class, 'pipelines-table')]//*[@data-pipeline]");
    }

    public boolean isFavorite(String job) {
        WebElement favorite = wait.until(driver -> {
            WebElement tr = driver.findElement(getSelectorForJob(job));
            return tr.findElement(By.cssSelector(".Checkbox.Favorite > label > input"));
        });
        return favorite.isSelected();
    }

    public void toggleFavorite(String job) {
        WebElement favorite = wait.until(driver -> {
            WebElement tr = driver.findElement(getSelectorForJob(job));
            return tr.findElement(By.cssSelector(".Checkbox.Favorite > label"));
        });

        favorite.click();

        if (isFavorite(job)) {
            logger.info(String.format("Pipeline %s was favorited", job));
        } else {
            logger.info(String.format("Pipeline %s was unfavorited", job));
        }
    }

    public void findJob(String jobName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(getSelectorForJob(jobName)));
    }

    public int getJobCount() {
        return driver.findElements(getSelectorForAllJobRows()).size();
    }

    public void testJobCountEqualTo(int numberOfJobs) {
        wait.until(ExpectedConditions.numberOfElementsToBe(
                getSelectorForAllJobRows(),
                numberOfJobs
        ));
        logger.info("found job count = " + numberOfJobs);
    }

    public void testJobCountAtLeast(int numberOfJobs) {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                getSelectorForAllJobRows(),
                numberOfJobs - 1
        ));
        logger.info("found job count >= " + numberOfJobs);
    }

    public void enterSearchText(String searchText) {
        WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-pipelines-input input"))
        );
        element.sendKeys(searchText);
        logger.info("entered search text =" + searchText);
    }

    public void clearSearchText() {
        WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-pipelines-input input"))
        );
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.BACK_SPACE);
        logger.info("cleared search text");
    }

    public void clickPipeline(String pipelineName){
        wait.until(By.xpath("//*/div[@data-pipeline='" + pipelineName + "']/a[1]")).click();
    }
}

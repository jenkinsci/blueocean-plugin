package io.blueocean.ath.pages.blue;

import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.WebDriverMixin;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DashboardPage implements WebDriverMixin {
    private Logger logger = LoggerFactory.getLogger(DashboardPage.class);

    @Inject
    public WaitUtil wait;

    public void open() {
        go("/blue/");
        logger.info("Navigated to dashboard page");
        wait.untilSSEReady();
    }

    public static By getSelectorForJob(String job) {
        return By.xpath("//*[@data-pipeline=\"" + job + "\"]");
    }

    public static By getSelectorForAllJobRows() {
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
            logger.info(String.format("AbstractPipeline %s was favorited", job));
        } else {
            logger.info(String.format("AbstractPipeline %s was unfavorited", job));
        }
    }

    public void findJob(String jobName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(getSelectorForJob(jobName)));
    }

    public int getJobCount() {
        return getDriver().findElements(getSelectorForAllJobRows()).size();
    }

    public void testJobCountEqualTo(int numberOfJobs) {
        wait.until(ExpectedConditions.numberOfElementsToBe(
                getSelectorForAllJobRows(),
                numberOfJobs
        ));
        logger.info("found job count = {}", numberOfJobs);
    }

    public void testJobCountAtLeast(int numberOfJobs) {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                getSelectorForAllJobRows(),
                numberOfJobs - 1
        ));
        logger.info("found job count >= {}", numberOfJobs);
    }

    public void enterSearchText(String searchText) {
        WebElement element = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-pipelines-input input"))
        );
        element.sendKeys(searchText);
        logger.info("entered search text = {}", searchText);
    }

    public void clearSearchText() {
        find(".search-pipelines-input input").clear();
        logger.info("cleared search text");
    }

    public void clickPipeline(String pipelineName){
        wait.until(By.xpath("//*/div[@data-pipeline='" + pipelineName + "']/a[1]")).click();
    }

    public void clickNewPipelineBtn() {
        open();
        find(".btn-new-pipeline").click();
        wait.until(ExpectedConditions.urlContains("create-pipeline"));
        logger.info("Clicked new pipeline");
    }
}

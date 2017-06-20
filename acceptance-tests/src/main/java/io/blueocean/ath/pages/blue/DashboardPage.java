package io.blueocean.ath.pages.blue;

import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.Pipeline;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Wait;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DashboardPage {
    private Logger logger = Logger.getLogger(DashboardPage.class);

    @Inject @BaseUrl
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

    public boolean isFavorite(String job) {
        WebElement favorite = wait.until(driver -> {
            WebElement tr = driver.findElement(By.xpath("//tr[@data-name=\"" + job + "\"]"));
            return tr.findElement(By.cssSelector(".Checkbox.Favorite > label > input"));
        });
        return favorite.isSelected();
    }

    public void toggleFavorite(String job) {
        WebElement favorite = wait.until(driver -> {
            WebElement tr = driver.findElement(By.xpath("//tr[@data-name=\"" + job + "\"]"));
            return tr.findElement(By.cssSelector(".Checkbox.Favorite > label"));
        });

        favorite.click();

        if (isFavorite(job)) {
            logger.info(String.format("Pipeline %s was favorited", job));
        } else {
            logger.info(String.format("Pipeline %s was unfavorited", job));
        }
    }

    public void clickPipeline(String pipelineName){
        wait.until(By.xpath("//*/tr[@data-name='" + pipelineName + "']/td/a[1]")).click();
    }
}

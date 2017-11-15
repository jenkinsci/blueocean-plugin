package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author cliffmeyers
 */
@Singleton
public class FavoritesDashboardPage extends DashboardPage {
    private Logger logger = Logger.getLogger(getClass());

    @Inject
    WaitUtil wait;

    WebElement getPipelineListItem(String jobName) {
        return wait.until(getSelectorForJob(jobName));
    }

    WebElement getPipelineListFavorite(String jobName) {
        return wait.until(driver -> {
            WebElement pipelineListItem = getPipelineListItem(jobName);
            return pipelineListItem.findElement(By.cssSelector(".Checkbox.Favorite > label"));
        });
    }

    WebElement getFavoriteCard(String fullName) {
        return wait.until(driver ->
            wait.until(By.cssSelector(".favorites-card-stack"))
                .findElement(By.cssSelector(".pipeline-card[data-full-name="+fullName.replace("/", "\\/")+"]"))
        );
    }

    public boolean isPipelineListItemFavorited(String jobName) {
        return getPipelineListFavorite(jobName)
            .findElement(By.cssSelector("input"))
            .isSelected();
    }

    public void togglePipelineListFavorite(String jobName) {
        logger.info(String.format("toggling favorite for %s", jobName));

        getPipelineListFavorite(jobName).click();

        if (isPipelineListItemFavorited(jobName)) {
            logger.info(String.format("job %s was favorited", jobName));
        } else {
            logger.info(String.format("job %s was unfavorited", jobName));
        }
    }

    public void removeFavoriteCard(String jobName) {
        getFavoriteCard(jobName)
            .findElement(By.cssSelector(".Checkbox.Favorite > label"))
            .click();
    }

    public void checkFavoriteCardCount(int quantity) {
        logger.info("checking favorite count = " + quantity);
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".favorites-card-stack .pipeline-card"), quantity));
    }

}

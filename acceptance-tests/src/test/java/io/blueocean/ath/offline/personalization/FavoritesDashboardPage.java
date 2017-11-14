package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.SmartWebElement;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cliffmeyers
 */
@Singleton
public class FavoritesDashboardPage extends DashboardPage {
    private Logger logger = Logger.getLogger(getClass());

    WebElement getPipelineListItem(String jobName) {
        return find(getSelectorForJob(jobName));
    }

    WebElement getFavoriteItem(String jobName) {
        WebElement pipelineListItem = getPipelineListItem(jobName);
        WebElement favoriteItem = pipelineListItem.findElement(By.cssSelector(".Checkbox.Favorite > label"));
        return favoriteItem;
    }

    List<WebElement> getAllFavoritedPipelineListItems() {
        return findNow(".pipelines-table .Checkbox.Favorite > label > input")
            .stream()
            .filter(WebElement::isSelected)
            .collect(Collectors.toList());
    }

    public boolean isPipelineListItemFavorited(String jobName) {
        return getFavoriteItem(jobName)
            .findElement(By.cssSelector("input"))
            .isSelected();
    }

    public void togglePipelineListItemFavorite(String jobName) {
        logger.info(String.format("toggling favorite for %s", jobName));
        getFavoriteItem(jobName).click();

        if (isPipelineListItemFavorited(jobName)) {
            logger.info(String.format("job %s was favorited", jobName));
        } else {
            logger.info(String.format("job %s was unfavorited", jobName));
        }
    }

    /*
    public WebElement failIntentionally(String jobName) {
        WebElement parentItem = getFavoriteItem(jobName);
        // WebElement parentItem = getPipelineListItem(jobName);
        WebElement result = parentItem.findElement(By.cssSelector(".foo"));
        return result;
    }
    */
}

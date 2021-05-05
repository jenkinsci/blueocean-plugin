package io.blueocean.ath.pages.blue;

import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.BlueJobStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cliffmeyers
 */
@Singleton
public class FavoritesDashboardPage extends DashboardPage {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    WaitUtil wait;

    public WebElement getFavoriteCard(String fullName) {
        WebElement stack = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".favorites-card-stack")));
        //stack.click();
        String pipelineName = fullName.replace("/", "\\/");
        List<WebElement> list = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(
            stack, By.cssSelector(".pipeline-card[data-full-name=" + pipelineName + "]")
        ));
        return list.get(0);
    }

    public boolean isPipelineListItemFavorited(String jobName) {
        return getPipelineListFavorite(jobName)
            .findElement(By.cssSelector("input"))
            .isSelected();
    }

    public void checkIsPipelineListItemFavorited(String jobName, boolean isFavorite) {
        WebElement favorite = getPipelineListFavorite(jobName)
            .findElement(By.cssSelector("input"));
        wait.until(ExpectedConditions.elementSelectionStateToBe(favorite, isFavorite));
    }

    public void togglePipelineListFavorite(String jobName) {
        logger.info("toggling favorite for {}", jobName);

        getPipelineListFavorite(jobName).click();

        if (isPipelineListItemFavorited(jobName)) {
            logger.info("job {} was favorited", jobName);
        } else {
            logger.info("job {} was unfavorited", jobName);
        }
    }

    public void checkFavoriteCardCount(int quantity) {
        logger.info("checking favorite count = {}", quantity);
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".favorites-card-stack .pipeline-card"), quantity), 30000);
    }

    public void clickFavoriteCardActivityLink(String fullName) {
        logger.info("clicking activity link for favorite {}", fullName);
        getFavoriteCard(fullName)
            .findElement(By.cssSelector(".name a"))
            .click();
    }

    public void clickFavoriteCardRunDetailsLink(String fullName) {
        logger.info("clicking run details link for favorite {}", fullName);
        getFavoriteCard(fullName)
            .click();
    }

    public WebElement checkFavoriteCardStatus(String fullName, BlueJobStatus ...statuses) {
        String statusi = Arrays.stream(statuses).map(Object::toString).collect(Collectors.joining(","));
        logger.info("waiting for status = {} for favorite card = {}", statusi, fullName);
        WebElement favorite = getFavoriteCard(fullName);
        Arrays.stream(statuses)
            .forEach(status -> wait.until(ExpectedConditions.attributeContains(favorite, "class", status.toString().toLowerCase()),
                                          15*1000));
        return favorite;
    }

    public void clickFavoriteCardRunButton(String fullName) {
        logger.info("clicking run button for {}", fullName);
        getFavoriteCard(fullName)
            .findElement(By.cssSelector("a.run-button"))
            .click();
    }

    public void clickFavoriteCardReplayButton(String fullName) {
        logger.info("clicking replay button for {}", fullName);
        getFavoriteCard(fullName)
            .findElement(By.cssSelector("a.replay-button"))
            .click();
    }

    public void removeFavoriteCard(String fullName) {
        logger.info("removing favorite for {}", fullName);
        getFavoriteCard(fullName)
            .findElement(By.cssSelector(".Checkbox.Favorite > label"))
            .click();
    }

    private WebElement getPipelineListItem(String jobName) {
        return wait.until(getSelectorForJob(jobName));
    }

    private WebElement getPipelineListFavorite(String jobName) {
        return wait.until(driver -> {
            WebElement pipelineListItem = getPipelineListItem(jobName);
            return pipelineListItem.findElement(By.cssSelector(".Checkbox.Favorite > label"));
        });
    }

}

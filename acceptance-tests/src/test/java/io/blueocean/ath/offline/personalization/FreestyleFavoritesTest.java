package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.Login;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.api.classic.ClassicJobApi;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;

/**
 * @author cliffmeyers
 */
@Login
@RunWith(ATHJUnitRunner.class)
public class FreestyleFavoritesTest implements WebDriverMixin {
    private Logger logger = Logger.getLogger(getClass());

    @Inject
    FavoritesDashboardPage dashboard;

    @Inject
    ClassicJobApi jobApi;

    @Test
    public void testFavorite() throws Exception {
        // NOTE: this is more or less a duplicate FavoritesTest where I am experimenting with new Selenium helpers
        String jobName = "favoriteJob";
        jobApi.createFreeStyleJob(jobName, "echo hi");
        dashboard.open();

        Assert.assertFalse(dashboard.isPipelineListItemFavorited(jobName));

        Assert.assertEquals(0, dashboard.getAllFavoritedPipelineListItems().size());

        dashboard.togglePipelineListItemFavorite(jobName);
        Assert.assertTrue(dashboard.isPipelineListItemFavorited(jobName));

        untilCondition(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".favorites-card-stack")));

        Assert.assertEquals(1, dashboard.getAllFavoritedPipelineListItems().size());

        dashboard.togglePipelineListItemFavorite(jobName);
        Assert.assertFalse(dashboard.isPipelineListItemFavorited(jobName));

        untilCondition(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".favorites-card-stack")));
    }
}

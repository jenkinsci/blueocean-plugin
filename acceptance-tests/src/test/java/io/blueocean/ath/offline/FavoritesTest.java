package io.blueocean.ath.offline;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.Login;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
@Login
@RunWith(ATHJUnitRunner.class)
public class FavoritesTest {
    private Logger logger = Logger.getLogger(FavoritesTest.class);
    @Inject
    DashboardPage dashboardPage;

    @Inject
    ClassicJobApi jobApi;

    /**
     * Makes sure that pipelines can be favorited.
     *
     * TODO: Add a test for the favorite cards.
     */
    @Test
    public void testFavorite() throws InterruptedException, UnirestException, IOException {

        String jobName = "favoriteJob";
        jobApi.createFreeStyleJob(jobName, "echo hi");
        dashboardPage.open();

        Assert.assertFalse(dashboardPage.isFavorite(jobName));

        dashboardPage.toggleFavorite(jobName);
        Assert.assertTrue(dashboardPage.isFavorite(jobName));

        dashboardPage.toggleFavorite(jobName);
        Assert.assertFalse(dashboardPage.isFavorite(jobName));
    }

}

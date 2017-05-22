package io.blueocean.ath;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.classic.LoginPage;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.IOException;

@Login
@RunWith(ATHJUnitRunner.class)
public class FavoritesTest{
    @Inject
    DashboardPage dashboardPage;

    @Inject
    ClassicJobApi jobApi;

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

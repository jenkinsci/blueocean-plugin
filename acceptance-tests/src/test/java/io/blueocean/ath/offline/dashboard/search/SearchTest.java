package io.blueocean.ath.offline.dashboard.search;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

@RunWith(ATHJUnitRunner.class)
public class SearchTest {
    private Logger logger = Logger.getLogger(SearchTest.class);

    @Inject
    DashboardPage dashboardPage;

    @Inject
    ClassicJobApi jobApi;

    @Test
    public void testSearch() throws InterruptedException, UnirestException, IOException {
        String alpha = "search-test-freestyle-alpha";
        String bravo = "search-test-freestyle-bravo";
        jobApi.createFreeStyleJob(alpha, "echo alpha");
        jobApi.createFreeStyleJob(bravo, "echo bravo");

        dashboardPage.open();
        dashboardPage.enterSearchText(alpha);
        dashboardPage.testJobCountEqualTo(1);
        dashboardPage.findJob(alpha);
        dashboardPage.clearSearchText();
        dashboardPage.testJobCountAtLeast(2);
    }
}

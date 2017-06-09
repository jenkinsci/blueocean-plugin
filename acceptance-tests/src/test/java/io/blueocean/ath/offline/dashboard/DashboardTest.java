package io.blueocean.ath.offline.dashboard;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;


@RunWith(ATHJUnitRunner.class)
public class DashboardTest {
    @Inject
    DashboardPage dashboardPage;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    WaitUtil wait;

    @Test
    public void testPipelineList() throws InterruptedException, UnirestException, IOException {
        jobApi.createFreeStyleJob("freestyle-alpha", "echo alpha");
        jobApi.createFreeStyleJob("freestyle-bravo", "echo bravo");
        dashboardPage.open();
        dashboardPage.testJobCountAtLeast(2);
    }
}

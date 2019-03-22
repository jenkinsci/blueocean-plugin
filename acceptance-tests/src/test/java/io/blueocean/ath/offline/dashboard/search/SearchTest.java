package io.blueocean.ath.offline.dashboard.search;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.offbytwo.jenkins.model.FolderJob;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.apache.log4j.Logger;
import org.junit.After;
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

    @After
    public void tearDown() throws IOException {
        // wipe out all jobs to avoid causing issues w/ SearchTest
        jobApi.deleteFolder("folder1");
    }
    @Test
    public void testSearch() throws InterruptedException, UnirestException, IOException {
        int totalJobs = 4;
        String alpha = "search-test-freestyle-alpha";
        String bravo = "search-test-freestyle-bravo";

        jobApi.createFreeStyleJob(alpha, "echo alpha");
        jobApi.createFreeStyleJob(bravo, "echo bravo");
        FolderJob jobFolder = jobApi.getFolder(Folder.folders("folder1", "folder2"), true);
        jobApi.createFreeStyleJob(jobFolder,"zz", "echo zz");
        jobApi.createFreeStyleJob(jobFolder,"zx", "echo zx");

        dashboardPage.open();
        dashboardPage.enterSearchText(alpha);
        dashboardPage.testJobCountEqualTo(1);
        dashboardPage.findJob(alpha);
        dashboardPage.clearSearchText();
        dashboardPage.testJobCountAtLeast(totalJobs);

        dashboardPage.enterSearchText("folder1/");
        dashboardPage.testJobCountEqualTo(2);
        dashboardPage.clearSearchText();
        dashboardPage.testJobCountAtLeast(totalJobs);

        dashboardPage.enterSearchText("f*/z*");
        dashboardPage.testJobCountEqualTo(2);
        dashboardPage.clearSearchText();
        dashboardPage.testJobCountAtLeast(totalJobs);


        dashboardPage.enterSearchText("z*");
        dashboardPage.testJobCountEqualTo(2);
        dashboardPage.clearSearchText();
        dashboardPage.testJobCountAtLeast(totalJobs);

        dashboardPage.enterSearchText("zz*");
        dashboardPage.testJobCountEqualTo(1);
        dashboardPage.clearSearchText();
        dashboardPage.testJobCountAtLeast(totalJobs);

        dashboardPage.enterSearchText("*search-test*");
        dashboardPage.testJobCountEqualTo(2);
        dashboardPage.clearSearchText();
        dashboardPage.testJobCountAtLeast(totalJobs);

    }
}

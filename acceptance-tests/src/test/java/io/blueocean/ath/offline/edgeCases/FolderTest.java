package io.blueocean.ath.offline.edgeCases;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.offbytwo.jenkins.model.FolderJob;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.pages.blue.RunDetailsArtifactsPage;
import io.blueocean.ath.pages.blue.RunDetailsPipelinePage;
import io.blueocean.ath.pages.blue.RunDetailsTestsPage;
import io.blueocean.ath.sse.SSEClientRule;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ATHJUnitRunner.class)
public class FolderTest extends BlueOceanAcceptanceTest {
    private Logger logger = Logger.getLogger(FolderTest.class);

    private static final Folder folder = Folder.folders("a folder", "bfolder", "cfolder");

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    @Inject @Rule
    public SSEClientRule client;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    WebDriver driver;

    @Inject @BaseUrl
    String base;

    @Inject
    WaitUtil wait;

    @Inject
    DashboardPage dashboardPage;

    ArrayList<Folder> createdFolders = new ArrayList();

    @After
    public void tearDown() throws IOException {
        // wipe out all jobs to avoid causing issues w/ SearchTest
        for (Folder folder : createdFolders) {
            jobApi.deleteFolder(folder.get(0));
        }
        createdFolders.clear();
    }

    /**
     * Tests that the activity page works when there are multiple layers of folders, and with funky characters.
     *
     * As long as activity loads run, any other page for this pipeline should load as it uses a shared router.
     */
    @Test
    public void multiBranchFolderTest() throws GitAPIException, IOException {
        String pipelineName = "FolderTest_multiBranchFolderTest";
        git.writeJenkinsFile(loadJenkinsFile());
        git.addAll();
        git.commit("First");
        git.createBranch("feature/1");

        MultiBranchPipeline p = mbpFactory.pipeline(folder, pipelineName).createPipeline(git);
        client.untilEvents(p.buildsFinished);
        ActivityPage activityPage = p.getActivityPage();
        activityPage.open();
        activityPage.checkBasicDomElements();
    }

    @Test
    public void foldersTest() throws IOException, GitAPIException, UnirestException, InterruptedException {
        String pipelineName = "Sohn";

        Folder folder = Folder.folders("firstFolder","三百", "ñba","七");
        FolderJob folderJob = jobApi.createFolders(folder, true);
        createdFolders.add(folder);

        jobApi.createFreeStyleJob(folderJob, pipelineName, "echo 'hello world!'");
        driver.get(folderJob.getUrl()+"/job/"+pipelineName+"/");
        driver.findElement(By.xpath("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".PlaceholderContent.NoRuns")));
        String activityPage = driver.getCurrentUrl();
        assertTrue(activityPage.endsWith(getNestedPipelinePath("firstFolder") +
                "Sohn/activity"));
        wait.until(By.cssSelector("nav a.activity"));
        wait.until(By.cssSelector("nav a.branches"));
        wait.until(By.cssSelector("nav a.pr"));
        wait.until(By.cssSelector("a.main_exit_to_app"));
        driver.findElement(By.cssSelector("a.main_exit_to_app")).click();
        wait.until(By.xpath("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']"));
        assertEquals(base+"/job/firstFolder/job/"+
                URLEncoder.encode("三百", "UTF-8")
                +"/job/"+URLEncoder.encode("ñba", "UTF-8")
                +"/job/"+URLEncoder.encode("七", "UTF-8")
                +"/job/Sohn/", driver.getCurrentUrl());
    }

    @Test
    public void anotherFoldersTest() throws IOException, GitAPIException, UnirestException {
        Folder anotherFolder =  Folder.folders("anotherFolder", "三百", "ñba", "七");
        FolderJob folderJob = jobApi.createFolders(anotherFolder, true);
        createdFolders.add(anotherFolder);

        git.writeJenkinsFile(loadJenkinsFile());
        git.writeFile("TEST-failure.TestThisWillFailAbunch.xml", loadResource("/TEST-failure.TestThisWillFailAbunch.xml"));
        git.writeFile("TEST-failure.TestThisWontFail.xml", loadResource("/TEST-failure.TestThisWontFail.xml"));
        git.addAll();
        git.commit("First");
        git.createBranch("feature/1");

        String pipelineName = "NestedFolderTest_multiBranchFolderTest";
        MultiBranchPipeline p = mbpFactory.pipeline(anotherFolder, pipelineName)
                .createPipeline(folderJob, git);
        client.untilEvents(p.buildsFinished);

        //check dashboard
        driver.get(base+"/blue/pipelines/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Header-topNav nav a[href=\"/blue/pipelines\"]")));
        driver.findElement(By.cssSelector(".pipelines-table"));
        driver.findElement(By.cssSelector(".Site-footer"));

        //validate MBP activity page

        //check dashboard has 1 run
        ActivityPage activityPage = p.getActivityPage().open();
        activityPage.testNumberRunsComplete(1, "unstable");

        //validate run details
        //JENKINS-36616 - Unable to load multibranch projects in a folder
        RunDetailsPipelinePage runDetails = p.getRunDetailsPipelinePage();
        runDetails.open("feature%2F1", 1);
        assertTrue(runDetails.checkTitle("feature/1"));
        assertEquals(0, runDetails.getDriver().findElements(By.className("a.authors")).size());
        runDetails.click(".ResultPageHeader-close");
        wait.tinySleep(1000);
        assertTrue(driver.findElements(By.cssSelector(".RunDetails-content")).size() == 0);


        //after closing we should be back to activity page
        assertEquals(base+getNestedPipelinePath("anotherFolder") +
                pipelineName+"/activity", driver.getCurrentUrl());

        //validate artifacts page
        RunDetailsArtifactsPage runDetailsArtifactsPage = p.getRunDetailsArtifactsPage();
        runDetailsArtifactsPage.open("feature/1", 1);
        runDetailsArtifactsPage.checkNumberOfArtifacts(3);

        //Check whether the test tab shows failing tests
        //@see {@link https://issues.jenkins-ci.org/browse/JENKINS-36674|JENKINS-36674} Tests are not being reported
        RunDetailsTestsPage testPage = p.getRunDetailsTestsPage();
        testPage.open("feature%2F1", 1);
        testPage.checkResults("failure", 3);

        //Jobs can be started from branch tab. - RUN
        //@see {@link https://issues.jenkins-ci.org/browse/JENKINS-36615|JENKINS-36615} the multibranch project has the branch 'feature/1'

        activityPage = p.getActivityPage().open();
        BranchPage branchPage = activityPage.clickBranchTab();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.run-button")));

        driver.findElement(By.cssSelector("a.run-button")).click();
        runDetails.open("feature%2F1", 2);
        client.untilEvents(p.buildsFinished);
        activityPage.open();
        activityPage.testNumberRunsComplete(2, "unstable");

        // test open blueocean from classic - run details
        // make sure the open blue ocean button works. In this case,
        // it should bring the browser to the run details page for the first run
        driver.get(base+"/job/anotherFolder/job/三百/job/ñba/job/七/job/"+pipelineName+"/job/feature%252F1/1/");
        wait.until(By.xpath("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']")).click();
        wait.until(ExpectedConditions.urlContains(getNestedPipelinePath("anotherFolder") + pipelineName + "/detail/feature%2F1/1/pipeline"));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".RunDetails-content .log-wrapper")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".RunDetails-content .Steps .logConsole"))
        ));

        // make sure the open blue ocean button works. In this case,
        // it should bring the browser to the main top-level pipelines page.
        // See https://issues.jenkins-ci.org/browse/JENKINS-39842
        driver.get(base+"/job/anotherFolder/job/三百/job/ñba");
        wait.until(By.xpath("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Header-topNav nav a[href=\"/blue/pipelines\"]")));
        driver.findElement(By.cssSelector(".pipelines-table"));
        driver.findElement(By.cssSelector(".Site-footer"));
        assertTrue(driver.getCurrentUrl().endsWith("/pipelines"));
    }

    private String getNestedPipelinePath(String rootFolder) throws UnsupportedEncodingException {
        return "/blue/organizations/jenkins/"+rootFolder+"%2F"
                + URLEncoder.encode("三百", "UTF-8")+ "%2F"
                + URLEncoder.encode("ñba", "UTF-8")+ "%2F"
                + URLEncoder.encode("七", "UTF-8")+ "%2F";
    }
}

package io.blueocean.ath.offline.edgeCases;

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
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
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

    private Folder folder = Folder.folders("a folder", "bfolder", "cfolder");

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
        p.getActivityPage().open();
    }

    @Test
    public void foldersTest() throws IOException, GitAPIException {
        String pipelineName = "Sohn";

        Folder folder = Folder.folders("firstFolder","三百", "ñba","七");
        FolderJob folderJob = jobApi.createFolders(folder, false);
        jobApi.createFreeStyleJob(folderJob, pipelineName, "echo 'hello world!'");
        driver.get(base);
        driver.findElement(By.xpath("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']")).click();
        driver.findElement(By.cssSelector(".PlaceholderContent.NoRuns"));
        assertTrue(driver.getCurrentUrl().endsWith("/blue/organizations/jenkins/firstFolder%2F三百%2Fñba%2F七%2FSohn/activity"));
    }

    @Test
    public void anotherFoldersTest() throws IOException, GitAPIException {
        Folder anotherFolder =  Folder.folders("anotherFolder", "三百", "ñba", "七");
        jobApi.createFolders(anotherFolder, true);

        git.writeJenkinsFile(loadJenkinsFile());
        git.addAll();
        git.commit("First");
        git.createBranch("feature/1");

        MultiBranchPipeline p = mbpFactory.pipeline(folder, "NestedFolderTest_multiBranchFolderTest").createPipeline(git);
        client.untilEvents(p.buildsFinished);

        //check dashboard
        driver.get(base+"/blue/pipelines/");
        driver.findElement(By.cssSelector(".Header-topNav nav a[href="+base+"/blue/pipelines/]"));
        driver.findElement(By.cssSelector(".pipelines-table"));
        driver.findElement(By.cssSelector(".Site-footer"));

        //validate MBP activity page

        //check dashboard has 1 run
        ActivityPage activityPage = p.getActivityPage().open();
        activityPage.testNumberRunsComplete(1);

        //validate run details
        //JENKINS-36616 - Unable to load multibranch projects in a folder
        RunDetailsPipelinePage runDetails = p.getRunDetailsPipelinePage();
        runDetails.open("feature/1", 1);
        assertTrue(runDetails.checkTitle("feature/1"));
        assertEquals(0, runDetails.getDriver().findElements(By.className("a.authors")).size());
        runDetails.click(".ResultPageHeader-close");

        //after closing we should be back to activity page
        assertEquals(base+"/blue/organizations/jenkins/blueocean/activity", driver.getCurrentUrl());

        //validate artifacts page
        RunDetailsArtifactsPage runDetailsArtifactsPage = p.getRunDetailsArtifactsPage();
        runDetailsArtifactsPage.open("feature/1", 1);
        runDetailsArtifactsPage.checkNumberOfArtifacts(1);

        //Check whether the test tab shows failing tests
        //@see {@link https://issues.jenkins-ci.org/browse/JENKINS-36674|JENKINS-36674} Tests are not being reported
        RunDetailsTestsPage testPage = p.getRunDetailsTestsPage();
        testPage.open("feature-1", 1);
        testPage.checkResults("failure", 2);

        //Jobs can be started from branch tab. - RUN
        //@see {@link https://issues.jenkins-ci.org/browse/JENKINS-36615|JENKINS-36615} the multibranch project has the branch 'feature/1'
        BranchPage branchPage = activityPage.clickBranchTab();
        wait.until(branchPage.find("a.run-button")).click();
        runDetails.open("feature/1", 2);
        client.untilEvents(p.buildsFinished);
        activityPage.open();
        activityPage.testNumberRunsComplete(2);

        // test open blueocean from classic - run details
        // make sure the open blue ocean button works. In this case,
        // it should bring the browser to the run details page for the first run
        driver.get(base+"anotherFolder/job/三百/job/ñba/job/七/job/Sohn/job/feature%252F1/1/");
        wait.until(By.xpath("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']")).click();
        assertTrue(driver.getCurrentUrl().endsWith("/blue/organizations/jenkins/anotherFolder%2F三百%2Fñba%2F七%2FSohn/detail/feature%2F1/1/pipeline"));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".RunDetails-content .log-wrapper")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".RunDetails-content .Steps .logConsole"))
        ));

        // make sure the open blue ocean button works. In this case,
        // it should bring the browser to the main top-level pipelines page.
        // See https://issues.jenkins-ci.org/browse/JENKINS-39842
        driver.get(base+"job/anotherFolder/job/三百/job/ñba");
        wait.until(By.xpath("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']")).click();
        assertTrue(driver.getCurrentUrl().endsWith("/blue/pipelines"));
        wait.until(By.cssSelector(".Header-topNav nav a[href="+base+"/blue/pipelines/]"));
        wait.until(driver.findElement(By.cssSelector(".pipelines-table")));
        wait.until(driver.findElement(By.cssSelector(".Site-footer")));
    }
}

package io.blueocean.ath.offline.multibranch;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.pages.blue.DashboardPage;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

/**
 * This class is for testing anything to do with click on links in Mulitbranch pipelines.
 */
@RunWith(ATHJUnitRunner.class)
public class NavigationTest extends BaseTest {

    @Rule @Inject
    public GitRepositoryRule git;

    @Inject
    DashboardPage dashboardPage;
    @Inject
    MultiBranchPipelineFactory multiBranchPipelineFactory;

    /**
     * This test clicks on the history button on a branch, and makes sure that it navigates to the activity page
     * on the correct url.
     */
    @Test
    public void testBranchHistory() throws IOException, GitAPIException {
        git.writeJenkinsFile(loadJenkinsFile());
        git.addAll();
        git.commit("Added Jenkinsfile");
        git.createBranch("feature/1");

        MultiBranchPipeline pipeline = multiBranchPipelineFactory.pipeline(Folder.folders("a folder", "bfolder", "folder"), "NavigationTest_testNavigation");
        pipeline.createPipeline(git);

        dashboardPage.open();
        dashboardPage.clickPipeline(pipeline.getName());

        ActivityPage activityPage = pipeline.getActivityPage().checkUrl();
        BranchPage branchPage = activityPage.clickBranchTab();

        branchPage.clickHistoryButton("feature/1");
    }
}

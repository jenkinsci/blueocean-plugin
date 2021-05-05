package io.blueocean.ath.live;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.CustomJenkinsServer;
import io.blueocean.ath.Login;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.pages.blue.EditorPage;
import io.blueocean.ath.pages.blue.GithubCreationPage;
import io.blueocean.ath.sse.SSEClientRule;
import io.blueocean.ath.util.GithubHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

@Login
@RunWith(ATHJUnitRunner.class)
public class GithubEditorTest {
    private Logger logger = LoggerFactory.getLogger(GithubEditorTest.class);

    @Inject
    GithubCreationPage creationPage;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    @Inject @Rule
    public SSEClientRule sseClient;

    @Inject
    EditorPage editorPage;

    @Inject
    WebDriver driver;

    @Inject
    CustomJenkinsServer jenkins;

    @Inject
    GithubHelper helper;

    /**
     * Cleans up repository after the test has completed.
     *
     * @throws IOException
     */
    @After
    public void deleteRepository() throws IOException {
        helper.cleanupRepository();
    }

    /**
     * Every test in this class gets a blank github repository created for them.
     *
     * @throws IOException
     */
    @Before
    public void createBlankRepo() throws IOException {
        helper.createEmptyRepository();
    }


    /**
     * This test covers e2e usage of the editor.
     *
     * Creates a blank github repo, and then uses editor to create a simple pipeline.
     */
    @Test
    public void testEditor() throws IOException {
        creationPage.createPipeline(helper.getAccessToken(), helper.getOrganizationOrUsername(), helper.getActualRepositoryName(), true);
        MultiBranchPipeline pipeline = mbpFactory.pipeline(helper.getActualRepositoryName());
        editorPage.simplePipeline();
        editorPage.saveBranch("main");
        ActivityPage activityPage = pipeline.getActivityPage().checkUrl();
        driver.navigate().refresh();
        sseClient.untilEvents(pipeline.buildsFinished);
        sseClient.clear();
        BranchPage branchPage = activityPage.clickBranchTab();
        branchPage.openEditor("main");
        editorPage.saveBranch("new - branch");
        activityPage.checkUrl();
        activityPage.getRunRowForBranch("new-branch");
        sseClient.untilEvents(pipeline.buildsFinished);
    }

    /**
     * This test covers creation of a pipeline, and changes agent settings within it.
     */
    @Test
    public void testEditorChangeAgentSetting() throws IOException {
        String newBranchName = "made-by-testEditorChangeAgentSetting";
        creationPage.createPipeline(helper.getAccessToken(), helper.getOrganizationOrUsername(), helper.getActualRepositoryName(), true);
        MultiBranchPipeline pipeline = mbpFactory.pipeline(helper.getActualRepositoryName());
        editorPage.simplePipeline();
        editorPage.saveBranch("main");
        ActivityPage activityPage = pipeline.getActivityPage().checkUrl();
        sseClient.untilEvents(pipeline.buildsFinished);
        sseClient.clear();
        BranchPage branchPage = activityPage.clickBranchTab();
        branchPage.openEditor("main");
        editorPage.setAgentLabel("none");
        editorPage.saveBranch(newBranchName);
        activityPage.checkUrl();
        activityPage.getRunRowForBranch(newBranchName);
        sseClient.untilEvents(pipeline.buildsFinished);
    }

    /**
     * This test covers creation of a pipeline, and subsequently adds a
     * stage within that same pipeline, then saves it to a new branch.
     */
    @Test
    public void testEditorAddAndDeleteStage() throws IOException {
        String firstBranchName = "branch-before-delete";
        String secondBranchName = "branch-after-delete";
        String stageToDelete = "stage to be deleted";
        creationPage.createPipeline(helper.getAccessToken(), helper.getOrganizationOrUsername(), helper.getActualRepositoryName(), true);
        MultiBranchPipeline pipeline = mbpFactory.pipeline(helper.getActualRepositoryName());
        editorPage.simplePipeline();
        editorPage.saveBranch("main");
        ActivityPage activityPage = pipeline.getActivityPage().checkUrl();
        sseClient.untilEvents(pipeline.buildsFinished);
        sseClient.clear();
        BranchPage branchPage = activityPage.clickBranchTab();
        branchPage.openEditor("main");
        editorPage.addStageToPipeline(pipeline, stageToDelete);
        editorPage.saveBranch(firstBranchName);
        activityPage.checkUrl();
        activityPage.getRunRowForBranch(firstBranchName);
        sseClient.untilEvents(pipeline.buildsFinished);
        sseClient.clear();
        branchPage.open();
        // The delete operations are here.
        branchPage.openEditor(firstBranchName);
        editorPage.deleteStage(stageToDelete);
        editorPage.saveBranch(secondBranchName);
        activityPage.checkUrl();
        activityPage.getRunRowForBranch(secondBranchName);
        sseClient.untilEvents(pipeline.buildsFinished);
    }

    /**
     * This test covers creation of a pipeline, and adds an environment
     * variable to it.
     */
    @Test
    public void testEditorSetEnvironmentVariables() throws IOException {
        String newBranchName = "made-by-testEditorSetEnvironmentVariables";
        creationPage.createPipeline(helper.getAccessToken(), helper.getOrganizationOrUsername(), helper.getActualRepositoryName(), true);
        MultiBranchPipeline pipeline = mbpFactory.pipeline(helper.getActualRepositoryName());
        editorPage.simplePipeline();
        editorPage.saveBranch("main");
        ActivityPage activityPage = pipeline.getActivityPage().checkUrl();
        sseClient.untilEvents(pipeline.buildsFinished);
        sseClient.clear();
        BranchPage branchPage = activityPage.clickBranchTab();
        branchPage.openEditor("main");
        editorPage.setEnvironmentVariable("NY_NEW_VAR", "MY_NEW_VALUE");
        editorPage.saveBranch(newBranchName);
        activityPage.checkUrl();
        activityPage.getRunRowForBranch(newBranchName);
        sseClient.untilEvents(pipeline.buildsFinished);
    }

    /**
     * Make sure we can paste a bad token that has whitespace added.
     */
    @Test
    public void testEditorWithSpace() throws IOException {
        // Gotta make Jenkins clear out its credential store or we might get a false positive depending on test order
        jenkins.deleteUserDomainCredential("alice", "blueocean-github-domain", "github");
        creationPage.createPipeline(" " + helper.getAccessToken() + " ", helper.getOrganizationOrUsername(), helper.getActualRepositoryName(), false);
    }

    /**
     * This test covers e2e usage of the editor. Does so with a parallel pipeline.
     *
     * Creates a blank github repo, and then uses editor to create a parallel pipeline.
     */
    @Test
    public void testEditorParallel() throws IOException {
        String branchNameForParallelPipeline = "branch-with-parallels";
        creationPage.createPipeline(helper.getAccessToken(), helper.getOrganizationOrUsername(), helper.getActualRepositoryName(), true);
        MultiBranchPipeline pipeline = mbpFactory.pipeline(helper.getActualRepositoryName());
        editorPage.parallelPipeline(4);
        editorPage.saveBranch(branchNameForParallelPipeline);
        ActivityPage activityPage = pipeline.getActivityPage().checkUrl();
        sseClient.untilEvents(pipeline.buildsFinished);
        sseClient.clear();
        activityPage.getRunRowForBranch("branch-with-parallels");
    }
}

package io.blueocean.ath.offline.multibranch;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.sse.SSEClientRule;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.junit.JGitTestUtil;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(ATHJUnitRunner.class)
public class CommitMessagesTest extends BlueOceanAcceptanceTest {
    private Logger logger = LoggerFactory.getLogger(CommitMessagesTest.class);

    @Rule public TestName name = new TestName();

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Rule
    @Inject
    public SSEClientRule sseClientRule;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    @Inject
    ClassicJobApi jobApi;

    @After
    public void tearDown() throws IOException {
        jobApi.deletePipeline(this.getClass().getSimpleName() + "_" + name.getMethodName());
    }
    /**
     * This tests the commit messages are being picked up from git and displayed on the run in activity.
     */
    @Test
    public void commitMessagesTest() throws IOException, GitAPIException {
        final String pipelineName = this.getClass().getSimpleName() + "_" + name.getMethodName();
        final String branchName = "master";

        URL jenkinsFile = getResourceURL("Jenkinsfile");
        Files.copy(Paths.get(jenkinsFile.getFile()), new File(git.gitDirectory, "Jenkinsfile").toPath());
        git.addAll();
        git.commit("initial commit");
        logger.info("Committed Jenkinsfile");

        MultiBranchPipeline pipeline = mbpFactory.pipeline(pipelineName).createPipeline(git);
        sseClientRule.untilEvents(pipeline.buildsFinished);
        sseClientRule.clear();

        JGitTestUtil.writeTrashFile(git.client.getRepository(), "trash", "hi");
        git.addAll();
        git.commit("2nd commit");

        logger.info("Committed a second time");

        pipeline.buildBranch(branchName);
        sseClientRule.untilEvents(pipeline.buildsFinished);

        ActivityPage activityPage = pipeline.getActivityPage().open();
        activityPage.checkForCommitMessage("2nd commit");

        // Do some assertions on the run data

        WebElement row = activityPage.getRunRowForBranch(branchName);
        List<WebElement> cells = row.findElements(activityPage.getSelectorForRowCells());
        assertEquals("Number of cells in row",8, cells.size());
        assertEquals("Branch label cell text", branchName, cells.get(3).getText());
        assertNotEquals("Run duration cell text", "-", cells.get(5).getText().trim());
        activityPage.assertIsDuration(cells.get(5).getText());
    }
}

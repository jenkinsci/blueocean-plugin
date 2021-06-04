package io.blueocean.ath.offline.multibranch;

import io.blueocean.ath.*;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.sse.SSEClientRule;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.junit.JGitTestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

@Login
@RunWith(ATHJUnitRunner.class)
public class StartStopTest extends BlueOceanAcceptanceTest {
    private Logger logger = LoggerFactory.getLogger(CommitMessagesTest.class);

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Rule
    @Inject
    public SSEClientRule sseClientRule;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    /**
     * This tests the ability to start and stop builds of a pipeline.
     */
    @Test
    public void startStopTest() throws IOException, GitAPIException {
        final String pipelineName = "StartStopTest_startStopTest";
        final String firstBranch = "master";
        final String secondBranch = "second-branch";

        URL navTestJenkinsfile = ParallelNavigationTest.class.getResource("StartStopTest/Jenkinsfile");
        Files.copy(new File(navTestJenkinsfile.getFile()).toPath(),
                new File(git.gitDirectory, "Jenkinsfile").toPath());
        git.addAll();
        git.commit("Initial commit of Jenkinsfile");
        logger.info("Committed Jenkinsfile");

        MultiBranchPipeline stopStartPipeline = mbpFactory.pipeline(pipelineName).createPipeline(git);
        sseClientRule.untilEvents(stopStartPipeline.buildsFinished);
        sseClientRule.clear();

        git.createBranch(secondBranch);
        JGitTestUtil.writeTrashFile(git.client.getRepository(), "some-new-filename", "Hello from startStopTest");
        git.addAll();
        git.commit("2nd commit going in now");
        logger.info("Committed a second time");

        // Fire a rescan to get the new branch to show up
        stopStartPipeline.rescanThisPipeline();
        sseClientRule.untilEvents(stopStartPipeline.buildsFinished);

        BranchPage branchPage = stopStartPipeline.getBranchPage().open();
        logger.info("Should have two branches now");
        branchPage.clickRunButton(firstBranch);
        branchPage.clickRunButton(secondBranch);
        branchPage.clickStopButton(firstBranch);
        branchPage.clickStopButton(secondBranch);
        ActivityPage activityPage = stopStartPipeline.getActivityPage().open();
        activityPage.checkPipeline();
        logger.info("Branches started and stopped successfully.");
    }
}

package io.blueocean.ath.offline.multibranch;


import com.google.common.io.Files;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.sse.SSEClientRule;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.junit.JGitTestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@RunWith(ATHJUnitRunner.class)
public class CommitMessagesTest extends BaseTest{
    private Logger logger = Logger.getLogger(CommitMessagesTest.class);

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Rule
    @Inject
    public SSEClientRule sseClientRule;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    /**
     * This tests the commit messages are being picked up from git and displayed on the run in activity.
     */
    @Test
    public void commitMessagesTest() throws IOException, GitAPIException {
        String pipelineName = "CommitMessagesTest_commitMessagesTest";

        URL jenkinsFile = getResourceURL("Jenkinsfile");
        Files.copy(new File(jenkinsFile.getFile()), new File(git.gitDirectory, "Jenkinsfile"));
        git.addAll();
        git.commit("initial commit");
        logger.info("Commited Jenkinsfile");

        MultiBranchPipeline pipeline = mbpFactory.pipeline(pipelineName).createPipeline(git);
        sseClientRule.untilEvents(pipeline.buildsFinished);
        sseClientRule.clear();

        JGitTestUtil.writeTrashFile(git.client.getRepository(), "trash", "hi");
        git.addAll();
        git.commit("2nd commit");

        logger.info("Commited a second time");

        pipeline.buildBranch("master");
        sseClientRule.untilEvents(pipeline.buildsFinished);

        ActivityPage activityPage = pipeline.getActivityPage().open();
        activityPage.checkForCommitMesssage("2nd commit");
    }
}

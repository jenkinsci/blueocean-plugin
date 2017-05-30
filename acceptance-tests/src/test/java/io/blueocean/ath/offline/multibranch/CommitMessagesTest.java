package io.blueocean.ath.offline.multibranch;


import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.FolderJob;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.AthModule;
import io.blueocean.ath.BaseTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.sse.SSEClient;
import io.blueocean.ath.sse.SSEEvents;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.junit.JGitTestUtil;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@RunWith(ATHJUnitRunner.class)
@UseModules(AthModule.class)
public class CommitMessagesTest extends BaseTest{
    private Logger logger = Logger.getLogger(CommitMessagesTest.class);

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Rule
    @Inject
    public SSEClient sseClient;

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
        sseClient.untilEvents(pipeline.buildsFinished);
        sseClient.clear();

        JGitTestUtil.writeTrashFile(git.client.getRepository(), "trash", "hi");
        git.addAll();
        git.commit("2nd commit");

        logger.info("Commited a second time");

        pipeline.buildBranch("master");
        sseClient.untilEvents(pipeline.buildsFinished);

        ActivityPage activityPage = pipeline.getActivityPage().open();
        activityPage.checkForCommitMesssage("2nd commit");
    }
}

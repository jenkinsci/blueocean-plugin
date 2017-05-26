package io.blueocean.ath.multibranch;


import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.FolderJob;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.AthModule;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.api.classic.ClassicJobApi;
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
public class CommitMessagesTest {
    private Logger logger = Logger.getLogger(CommitMessagesTest.class);

    @Rule
    public GitRepositoryRule repo = new GitRepositoryRule();

    @Rule @Inject
    public SSEClient sseClient;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    JenkinsServer jenkins;

    @Inject
    ActivityPage activityPage;

    @Test
    public void commitMessagesTest () throws IOException, GitAPIException {
        String pipelineName = "CommitMessagesTest_tested";

        URL jenkinsFile = Resources.getResource(CommitMessagesTest.class, "CommitMessagesTest/Jenkinsfile");
        Files.copy(new File(jenkinsFile.getFile()), new File(repo.gitDirectory, "Jenkinsfile"));
        repo.git.add().addFilepattern(".").call();
        repo.git.commit().setMessage("initial commit").call();
        logger.info("Commited Jenkinsfile");

        jobApi.createMultlBranchPipeline(pipelineName, repo.gitDirectory.getAbsolutePath());

        sseClient.untilEvents(SSEEvents.activityComplete(pipelineName));
        sseClient.clear();

        JGitTestUtil.writeTrashFile(repo.git.getRepository(), "trash", "hi");
        repo.git.add().addFilepattern(".").call();
        repo.git.commit().setMessage("2nd commit").call();
        logger.info("Commited a second time");

        Optional<FolderJob> folderJob = jenkins.getFolderJob(jenkins.getJob(pipelineName));

        jenkins.getJob(folderJob.get(), "master").build();
        sseClient.untilEvents(SSEEvents.activityComplete(pipelineName));

        activityPage.open(pipelineName);
        activityPage.checkForCommitMesssage("2nd commit");
    }
}

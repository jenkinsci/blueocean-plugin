package io.blueocean.ath.offline.multibranch;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.sse.SSEClientRule;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

@RunWith(ATHJUnitRunner.class)
public class RestartStageTest extends BlueOceanAcceptanceTest {
    private Logger logger = LoggerFactory.getLogger(RestartStageTest.class);

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Rule
    @Inject
    public SSEClientRule sseClientRule;

    @Inject
    WaitUtil wait;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    /**
     * This tests the restart stage functionality in the run details screen.
     */
    @Test
    @Ignore("Super flakey test")
    public void restartStageTest() throws IOException, GitAPIException, InterruptedException {
        final String pipelineName = "RestartStageTest";
        final String branchName = "master";
        URL jenkinsFile = RestartStageTest.class.getResource("RestartStageTest/Jenkinsfile");
        Files.copy(new File(jenkinsFile.getFile()).toPath(), new File(git.gitDirectory, "Jenkinsfile").toPath());

        git.addAll();
        git.commit("Initial commit for " + pipelineName);
        logger.info("Committed Jenkinsfile for " + pipelineName);
        MultiBranchPipeline pipeline = mbpFactory.pipeline(pipelineName).createPipeline(git);
        logger.info("Finished creating " + pipelineName);

        logger.info("Beginning restartStageTest()");
        pipeline.getRunDetailsPipelinePage().open(1);
        sseClientRule.untilEvents(pipeline.buildsFinished);
        sseClientRule.clear();

        //find the restart stage link and click it
        wait.click(By.xpath("//*[contains(@class, 'restart-stage')]"));

        //wait until new build finishes
        sseClientRule.untilEvents(pipeline.buildsFinished);

        //check that there is one stage that was not built
        wait.until(By.cssSelector(".PWGx-svgResultStatusOutline"));

        //check that there is one stage that has been successfully built
        wait.until(By.cssSelector(".PWGx-pipeline-node-selected .circle-bg.success"));
    }
}

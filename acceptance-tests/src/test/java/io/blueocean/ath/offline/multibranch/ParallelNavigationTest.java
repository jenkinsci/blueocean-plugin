package io.blueocean.ath.offline.multibranch;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.Retry;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.sse.SSEClientRule;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@RunWith(ATHJUnitRunner.class)
public class ParallelNavigationTest {

    private Logger logger = LoggerFactory.getLogger(CommitMessagesTest.class);

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Rule
    @Inject
    public SSEClientRule sseClientRule;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    WaitUtil wait;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    // Names of the pipelines we'll create
    String navTest = "ParallelNavTest_tested";
    String navTestWithInput = "ParallelNavTestWithInput_tested";
    String navTestWithFailedInputStep = "ParallelNavigationTest_failed_input";
    String navTestWithNoStepsNoStages = "ParallelNavTestWithNoStepsNoStages";
    String navTestInputParamGoToClassicLink = "ParallelNavTestInputParamGoToClassicLink";

    // Initialize our MultiBranchPipeline objects
    static MultiBranchPipeline navTestPipeline = null;
    static MultiBranchPipeline navTestWithInputPipeline = null;
    static MultiBranchPipeline navTestWithFailedInputStepPipeline = null;
    static MultiBranchPipeline navTestWithNoStepsNoStagesPipeline = null;
    static MultiBranchPipeline navTestInputParamGoToClassicLinkPipeline = null;

    /**
     * This checks that we can run a pipeline with 2 long running parallel branches.
     * You should be able to click between one and the other and see it progressing.
     */
    @Test
    @Retry(3)
    public void parallelNavigationTest() throws IOException, GitAPIException, InterruptedException {
        // Create navTest
        logger.info("Creating pipeline {}", navTest);
        URL navTestJenkinsfile = Resources.getResource(ParallelNavigationTest.class, "ParallelNavigationTest/Jenkinsfile");
        Files.copy(new File(navTestJenkinsfile.getFile()), new File(git.gitDirectory, "Jenkinsfile"));
        git.addAll();
        git.commit("Initial commit for " + navTest);
        logger.info("Committed Jenkinsfile for {}", navTest);
        navTestPipeline = mbpFactory.pipeline(navTest).createPipeline(git);
        logger.info("Finished creating {}", navTest);

        logger.info("Beginning parallelNavigationTest()");
        navTestPipeline.getRunDetailsPipelinePage().open(1);
        // At first we see branch one
        wait.until(By.xpath("//*[text()=\"firstBranch\"]"));
        logger.info("Found first branch");
        wait.until(By.xpath("//*[text()=\"first branch visible\"]"));
        // and clicking on the unselected node will yield us the second branch
        wait.click(By.xpath("//*[contains(@class, 'pipeline-node')][3]"));
        wait.until(By.xpath("//*[text()=\"secondBranch\"]"));
        wait.until(By.xpath("//*[text()=\"second branch visible\"]"));
        logger.info("Found second branch");
    }

    /**
     * This checks that you can have input on 2 different parallel branches. There are no stages either side of the parallel construct.
     * One at a time, the proceed button will be clicked.
     */
    @Test
    public void parallelNavigationTestInput() throws IOException, GitAPIException, InterruptedException {
        // Create navTestWithInput
        logger.info("Creating pipeline {}", navTestWithInput);
        URL navTestInputJenkinsfile = Resources.getResource(ParallelNavigationTest.class, "ParallelNavigationTest/Jenkinsfile.input");
        Files.copy(new File(navTestInputJenkinsfile.getFile()), new File(git.gitDirectory, "Jenkinsfile"));
        git.addAll();
        git.commit("Initial commit for " + navTestWithInput);
        logger.info("Committed Jenkinsfile for " + navTestWithInput);
        navTestWithInputPipeline = mbpFactory.pipeline(navTestWithInput).createPipeline(git);
        logger.info("Finished creating {}", navTestWithInput);

        logger.info("Beginning parallelNavigationTestInput()");
        navTestWithInputPipeline.getRunDetailsPipelinePage().open(1);
        // At first we see branch one
        wait.until(By.xpath("//*[text()=\"firstBranch\"]"));
        logger.info("Found first branch");
        wait.click(By.cssSelector(".btn.inputStepSubmit"));
        logger.info("Clicked the inputStepSubmit button");
        // And clicking on the unselected node will yield us the second branch
        wait.click(By.xpath("//*[contains(@class, 'pipeline-node')][3]"));
        wait.until(By.xpath("//*[text()=\"secondBranch\"]"));
        logger.info("Found second branch");
        wait.click(By.cssSelector(".btn.inputStepSubmit"));
        logger.info("Clicked the inputStepSubmit button");
    }

    /**
     * This checks that an error is shown in the UI for a failed input step
     */
    @Test
    public void failedInputStep() throws IOException, GitAPIException, InterruptedException {
        try
        {
            // Create navTestWithFailedInputStep
            logger.info("Creating pipeline {}", navTestWithFailedInputStep);
            URL navTestWithFailedInputStepJenkinsfile = Resources.getResource(ParallelNavigationTest.class,
                                                                               "ParallelNavigationTest/Jenkinsfile.failed.input");
            Files.copy(new File( navTestWithFailedInputStepJenkinsfile.getFile()),
                        new File(git.gitDirectory, "Jenkinsfile"));
            git.addAll();
            git.commit("Initial commit for " + navTestWithFailedInputStep);
            logger.info("Committed Jenkinsfile for {}", navTestWithFailedInputStep);
            navTestWithFailedInputStepPipeline = mbpFactory.pipeline(navTestWithFailedInputStep).createPipeline(git);
            logger.info("Finished creating " + navTestWithFailedInputStep);

            navTestWithFailedInputStepPipeline.getRunDetailsPipelinePage().open(1);

            logger.info("Beginning failedInputStep()");
            wait.until(
                By.xpath("//*[text()=\"This step will fail because the user is not authorised to click OK\"]"));
            logger.info("Found failed input step error message");
            //wait.until(By.cssSelector( ".btn.inputStepSubmit" ) ).click();
            wait.until(By.xpath("//button[@class='btn inputStepSubmit']")).click();
            logger.info("Clicked the inputStepSubmit button");
        } finally {
            mbpFactory.pipeline(navTestWithFailedInputStep).deleteThisPipeline(navTestWithFailedInputStep);
        }

    }

    /**
     * This checks that the log is visible when a run fails with no steps or stages
     */
    @Test
    public void testLogVisibilityWhenNoStepsOrStages() throws IOException, GitAPIException, InterruptedException {
        // Create navTestWithNoStepsNoStages
        logger.info("Creating pipeline " + navTestWithNoStepsNoStages);
        URL navTestWithNoStepsNoStagesJenkinsfile = Resources.getResource(ParallelNavigationTest.class, "ParallelNavigationTest/Jenkinsfile.nosteps.nostages");
        Files.copy(new File(navTestWithNoStepsNoStagesJenkinsfile.getFile()), new File(git.gitDirectory, "Jenkinsfile"));
        git.addAll();
        git.commit("Initial commit for " + navTestWithNoStepsNoStages);
        logger.info("Committed Jenkinsfile for " + navTestWithNoStepsNoStages);
        navTestWithNoStepsNoStagesPipeline = mbpFactory.pipeline(navTestWithNoStepsNoStages).createPipeline(git);
        sseClientRule.untilEvents(navTestWithNoStepsNoStagesPipeline.buildsFinished);
        logger.info("Finished creating " + navTestWithNoStepsNoStages);

        navTestWithNoStepsNoStagesPipeline.getRunDetailsPipelinePage().open(1);

        logger.info("Wait for log to appear");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".log-body")));
    }

    /**
     * This checks that href attr of the alert error for unsupported inputs leads to the correct classic url
     */
    @Test
    public void testInputParamGoToClassicLink() throws IOException, GitAPIException, InterruptedException {
        // Create navTestInputParamGoToClassicLink
        logger.info("Creating pipeline " + navTestInputParamGoToClassicLink);
        URL navTestInputParamGoToClassicLinkJenkinsfile = Resources.getResource(ParallelNavigationTest.class, "ParallelNavigationTest/Jenkinsfile.input.param.classic.link");
        Files.copy(new File(navTestInputParamGoToClassicLinkJenkinsfile.getFile()), new File(git.gitDirectory, "Jenkinsfile"));
        git.addAll();
        git.commit("Initial commit for " + navTestInputParamGoToClassicLink);
        logger.info("Committed Jenkinsfile for " + navTestInputParamGoToClassicLink);
        navTestInputParamGoToClassicLinkPipeline = mbpFactory.pipeline(navTestInputParamGoToClassicLink).createPipeline(git);
        logger.info("Finished creating " + navTestInputParamGoToClassicLink);

        navTestInputParamGoToClassicLinkPipeline.getRunDetailsPipelinePage().open(1);

        logger.info("Wait for alert error with link to classic input to appear");
        wait.until(By.xpath("//*[@class=\"Alert Error\"]//a[@href=\"/job/ParallelNavTestInputParamGoToClassicLink/job/master/1/input\"]"));
    }
}

package io.blueocean.ath.offline.multibranch;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.AthModule;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.pages.blue.ResultsPage;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@RunWith(ATHJUnitRunner.class)
@UseModules(AthModule.class)
public class ParallelNavigationTest {

    private Logger logger = Logger.getLogger(CommitMessagesTest.class);

    @Rule
    public GitRepositoryRule repo = new GitRepositoryRule();

    @Inject
    ClassicJobApi jobApi;

    @Inject
    ResultsPage resultsPage;

    @Inject
    WaitUtil wait;


    /**
     * This checks that we can run a pipeline with 2 long running parallel branches.
     * You should be able to click between one and the other and see it progressing.
     */
    @Test
    public void parallelNavigationTest () throws IOException, GitAPIException, InterruptedException {
        String pipelineName = "ParallelNavigationTest_tested";

        URL jenkinsFile = Resources.getResource(ParallelNavigationTest.class, "ParallelNavigationTest/Jenkinsfile");
        Files.copy(new File(jenkinsFile.getFile()), new File(repo.gitDirectory, "Jenkinsfile"));
        repo.git.add().addFilepattern(".").call();
        repo.git.commit().setMessage("initial commit").call();
        logger.info("Commited Jenkinsfile");

        jobApi.createMultlBranchPipeline(pipelineName, repo.gitDirectory.getAbsolutePath());

        resultsPage.open(pipelineName, 1);

        // at first we see branch one
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()=\"Steps firstBranch\"]")));

        // and clicking on the unselected node will yield us the second branch
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".pipeline-node"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()=\"Steps secondBranch\"]")));


    }


    /**
     * This checks that you can have input on 2 different parallel branches. There are no stages either side of the parallel construct.
     * One at a time, the proceed button will be clicked.
     */
    @Test
    public void parallelNavigationTestInput () throws IOException, GitAPIException, InterruptedException {
        String pipelineName = "ParallelNavigationTest_tested_input";

        URL jenkinsFile = Resources.getResource(ParallelNavigationTest.class, "ParallelNavigationTest/Jenkinsfile.input");
        Files.copy(new File(jenkinsFile.getFile()), new File(repo.gitDirectory, "Jenkinsfile"));
        repo.git.add().addFilepattern(".").call();
        repo.git.commit().setMessage("initial commit").call();
        logger.info("Commited Jenkinsfile");

        jobApi.createMultlBranchPipeline(pipelineName, repo.gitDirectory.getAbsolutePath());

        resultsPage.open(pipelineName, 1);

        // at first we see branch one
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()=\"Steps firstBranch\"]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".btn.inputStepSubmit"))).click();

        // and clicking on the unselected node will yield us the second branch
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".pipeline-node"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text()=\"Steps secondBranch\"]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".btn.inputStepSubmit"))).click();


    }


}

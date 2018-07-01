package io.blueocean.ath.offline.multibranch;

import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.RunDetailsTestsPage;
import io.blueocean.ath.sse.SSEClientRule;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.IOException;

@RunWith(ATHJUnitRunner.class)
public class TestResultsErrorStdOutTest
    extends BlueOceanAcceptanceTest{

    @Inject
    @Rule
    public SSEClientRule sseClient;

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    MultiBranchPipeline pipeline;

    @Before
    public void setup() throws IOException, GitAPIException {
        git.writeJenkinsFile(loadJenkinsFile());
        git.writeFile("TEST-io.blueocean.StdoutStderr.xml" ,loadResource("TEST-io.blueocean.StdoutStderr.xml"));
        git.writeFile("TEST-io.blueocean.NoErrorMessage.xml" ,loadResource("TEST-io.blueocean.NoErrorMessage.xml"));
        git.writeFile("TEST-io.blueocean.Success.xml" ,loadResource("TEST-io.blueocean.Success.xml"));

        git.addAll();
        git.commit("First");

        pipeline = mbpFactory.pipeline("TestResultsErrorStdOutTest").createPipeline(git);
        sseClient.untilEvents(pipeline.buildsFinished);
    }

    @Test
    public void std_out_visible(){
        RunDetailsTestsPage runDetailsTestsPage = pipeline.getRunDetailsTestsPage().open("master", 1);
        runDetailsTestsPage.getWaitUntil().click( By.xpath( "//span[contains(text(), 'io.blueocean.TestResults.StdOut')]" ) );
        runDetailsTestsPage.getWaitUntil().until( By.xpath( "//h4[contains(text(), 'Stacktrace')]") );
        runDetailsTestsPage.getWaitUntil().until( By.xpath( "//h4[contains(text(), 'Standard Error')]"));
        runDetailsTestsPage.getWaitUntil().until( By.xpath( "//h4[contains(text(), 'Standard Output')]"));
        runDetailsTestsPage.getWaitUntil().until( By.xpath( "//span[@class='line-content' and contains(text(), 'stdout msg')]") );

        runDetailsTestsPage.getWaitUntil().click( By.xpath( "//span[contains(text(), 'io.jenkins.blueocean.commons.ExportTest')]" ) );
        runDetailsTestsPage.getWaitUntil().until( By.xpath( "//span[@class='line-content' and contains(text(), 'success_stdout_msg')]") );
        runDetailsTestsPage.getWaitUntil().until( By.xpath( "//span[@class='line-content' and contains(text(), 'success_stderr_msg')]") );

    }
}

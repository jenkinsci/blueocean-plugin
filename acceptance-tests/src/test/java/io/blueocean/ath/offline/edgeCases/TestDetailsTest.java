package io.blueocean.ath.offline.edgeCases;

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

import java.io.IOException;

@RunWith(ATHJUnitRunner.class)
public class TestDetailsTest extends BlueOceanAcceptanceTest{

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
        git.writeFile("TEST-failure.TestThisWillFailAbunch.xml" ,loadResource("TEST-failure.TestThisWillFailAbunch.xml"));
        git.writeFile("TEST-failure.TestThisWontFail.xml" ,loadResource("TEST-failure.TestThisWontFail.xml"));
        git.addAll();
        git.commit("First");

        pipeline = mbpFactory.pipeline("TestDetailsTest").createPipeline(git);
        sseClient.untilEvents(pipeline.buildsFinished);
    }

    @Test
    public void testTests(){
        RunDetailsTestsPage runDetailsTestsPage = pipeline.getRunDetailsTestsPage().open("master", 1);
        runDetailsTestsPage.checkResults("success", 10);
    }
}

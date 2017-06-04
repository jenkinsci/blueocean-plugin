package io.blueocean.ath.offline.edgeCases;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.DashboardPage;
import io.blueocean.ath.sse.SSEClientRule;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.IOException;

@RunWith(ATHJUnitRunner.class)
public class FolderTest extends BaseTest {
    private Logger logger = Logger.getLogger(FolderTest.class);

    private Folder folder = Folder.folders("a folder", "bfolder", "cfolder");

    @Rule
    @Inject
    public GitRepositoryRule git;

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    @Inject @Rule
    public SSEClientRule client;

    /**
     * Tests that the activity page works when there are multiple layers of folders, and with funky characters.
     *
     * As long as activity loads run, any other page for this pipeline should load as it uses a shared router.
     */
    @Test
    public void multiBranchFolderTest() throws GitAPIException, IOException {
        String pipelineName = "FolderTest_multiBranchFolderTest";
        git.writeJenkinsFile(loadJenkinsFile());
        git.addAll();
        git.commit("First");
        git.createBranch("feature/1");

        MultiBranchPipeline p = mbpFactory.pipeline(folder, pipelineName).createPipeline(git);
        client.untilEvents(p.buildsFinished);
        p.getActivityPage().open();
    }
}

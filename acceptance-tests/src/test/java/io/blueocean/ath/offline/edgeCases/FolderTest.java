package io.blueocean.ath.offline.edgeCases;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.AthModule;
import io.blueocean.ath.BaseTest;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.factory.PipelineFactory;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.model.Pipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URLEncoder;

@RunWith(ATHJUnitRunner.class)
@UseModules(AthModule.class)
public class FolderTest extends BaseTest {
    private Logger logger = Logger.getLogger(FolderTest.class);
    @Rule
    @Inject
    public GitRepositoryRule git;

    @Inject
    ClassicJobApi jobApi;

    String[] folders = {"aFfolder", "b Folder", "cFolder"};

    @Inject
    MultiBranchPipelineFactory mbpFactory;

    @Inject
    WebDriver driver;

    @Test
    public void multiBranchFolderTest() throws GitAPIException, IOException {
        String pipelineName = "FolderTest_multiBranchFolderTest";
        git.writeJenkinsFile(loadJenkinsFile());
        git.client.add().addFilepattern(".").call();
        git.client.commit().setMessage("bah").call();
        git.createBranch("feature/1");
        MultiBranchPipeline p = null;
        try {
            p = mbpFactory
                .pipeline(
                    Folder.folders("aFold er", URLEncoder.encode("bF older"), "cFolder"),
                    pipelineName)
                .createPipeline(git);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        ActivityPage activityPage = p.getActivityPage();
        activityPage.open();


        logger.info(driver.getCurrentUrl());
    }



}

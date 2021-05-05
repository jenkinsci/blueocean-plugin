package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author cliffmeyers
 */
public class FavoritesAddRemoveTest extends AbstractFavoritesTest {
    private static final Logger logger = LoggerFactory.getLogger(FavoritesAddRemoveTest.class);

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Test
    public void testFreestyle() throws IOException {
        String jobName = "addremove-freestyle";
        FreestyleJob freestyle = freestyleFactory.pipeline(FOLDER, jobName).create("echo hi");
        String fullName = freestyle.getFullName();

        dashboardPage.open();
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(1);
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(0);
        Assert.assertTrue("should not be favorited", !dashboardPage.isPipelineListItemFavorited(jobName));
    }

    @Test
    public void testClassicPipeline() throws IOException {
        String jobName = "addremove-classic-pipeline";
        String script = resources.loadJenkinsFile();
        ClassicPipeline pipeline = pipelineFactory.pipeline(FOLDER, jobName).createPipeline(script).build();
        String fullName = pipeline.getFullName();

        dashboardPage.open();
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(1);
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(0);
        Assert.assertTrue("should not be favorited", !dashboardPage.isPipelineListItemFavorited(jobName));
    }

    @Test
    public void testMultibranch() throws IOException, GitAPIException {
        git.writeJenkinsFile(resources.loadJenkinsFile());
        git.addAll();
        git.commit("First");
        git.createBranch("feature/1");

        String jobName = "addremove-multibranch";
        MultiBranchPipeline pipeline = multibranchFactory.pipeline(FOLDER, jobName).createPipeline(git);
        String fullName = pipeline.getFullName();

        dashboardPage.open();
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(1);
        dashboardPage.getFavoriteCard(fullName).findElement(By.cssSelector(".branchText[title=master]"));
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(0);
        Assert.assertTrue("should not be favorited", !dashboardPage.isPipelineListItemFavorited(jobName));
    }
}

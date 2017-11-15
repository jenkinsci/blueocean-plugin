package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.Login;
import io.blueocean.ath.ResourceResolver;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.jenkins.blueocean.util.HttpRequest;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author cliffmeyers
 */
@Login
@RunWith(ATHJUnitRunner.class)
public class FavoritesAddRemoveTest implements WebDriverMixin {
    private static final Logger logger = Logger.getLogger(FavoritesAddRemoveTest.class);
    private static final Folder FOLDER = new Folder("personalization-folder");

    @Rule @Inject
    public GitRepositoryRule git;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    FavoritesDashboardPage dashboard;

    @Inject
    FreestyleJobFactory freestyleFactory;

    @Inject
    ClassicPipelineFactory pipelineFactory;

    @Inject
    MultiBranchPipelineFactory multibranchFactory;

    @Inject @BaseUrl
    String base;

    private ResourceResolver resources;

    private HttpRequest httpRequest() {
        return new HttpRequest(base + "/blue/rest");
    }

    @Before
    public void setUp() throws IOException {
        resources = new ResourceResolver(getClass());

        String user = "alice";
        logger.info(String.format("deleting any existing favorites for %s", user));

        httpRequest()
            .Delete("/users/{user}/favorites/")
            .urlPart("user", user)
            .auth(user, user)
            .status(204)
            .as(Void.class);
    }

    @After
    public void tearDown() throws IOException {
        jobApi.deleteFolder(FOLDER);
    }

    @Test
    public void testFreestyle() throws IOException {
        String jobName = "addremove-freestyle";
        FreestyleJob freestyle = freestyleFactory.pipeline(FOLDER, jobName).create("echo hi");
        String fullName = freestyle.getFullName();

        dashboard.open();
        dashboard.togglePipelineListItemFavorite(jobName);
        dashboard.checkFavoriteCardCount(1);
        dashboard.removeFavoriteCard(fullName);
        dashboard.checkFavoriteCardCount(0);
        dashboard.isPipelineListItemFavorited(jobName);
        Assert.assertTrue("should not be favorited", !dashboard.isPipelineListItemFavorited(jobName));
    }

    @Test
    public void testClassicPipeline() throws IOException {
        String jobName = "addremove-classic-pipeline";
        String script = resources.loadJenkinsFile();
        ClassicPipeline pipeline = pipelineFactory.pipeline(FOLDER, jobName).createPipeline(script).build();
        String fullName = pipeline.getFullName();

        dashboard.open();
        dashboard.togglePipelineListItemFavorite(jobName);
        dashboard.checkFavoriteCardCount(1);
        dashboard.removeFavoriteCard(fullName);
        dashboard.checkFavoriteCardCount(0);
        Assert.assertTrue("should not be favorited", !dashboard.isPipelineListItemFavorited(jobName));
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

        dashboard.open();
        dashboard.togglePipelineListItemFavorite(jobName);
        dashboard.checkFavoriteCardCount(1);
        dashboard.getFavoriteCard(fullName).findElement(By.cssSelector(".branchText[title=master]"));
        dashboard.removeFavoriteCard(fullName);
        dashboard.checkFavoriteCardCount(0);
        Assert.assertTrue("should not be favorited", !dashboard.isPipelineListItemFavorited(jobName));
    }

}

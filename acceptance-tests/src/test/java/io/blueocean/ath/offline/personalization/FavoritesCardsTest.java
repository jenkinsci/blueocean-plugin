package io.blueocean.ath.offline.personalization;

import com.google.common.collect.ImmutableList;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.GitRepositoryRule;
import io.blueocean.ath.Login;
import io.blueocean.ath.ResourceResolver;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.factory.MultiBranchPipelineFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.FavoritesDashboardPage;
import io.jenkins.blueocean.util.HttpRequest;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static io.blueocean.ath.model.BlueJobStatus.RUNNING;
import static io.blueocean.ath.model.BlueJobStatus.SUCCESS;

/**
 * @author cliffmeyers
 */
@Login
@RunWith(ATHJUnitRunner.class)
public class FavoritesCardsTest implements WebDriverMixin {
    private static final Logger logger = Logger.getLogger(FavoritesAddRemoveTest.class);
    private static final Folder FOLDER = new Folder("personalization-folder");

    @Rule @Inject
    public GitRepositoryRule git;

    @Inject @BaseUrl
    String base;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    FreestyleJobFactory freestyleFactory;

    @Inject
    ClassicPipelineFactory pipelineFactory;

    @Inject
    MultiBranchPipelineFactory multibranchFactory;

    @Inject
    FavoritesDashboardPage dashboardPage;

    @Inject
    ActivityPageFactory activityPageFactory;

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
        // wipe out all jobs to avoid causing issues w/ SearchTest
        jobApi.deleteFolder(FOLDER);
    }

    @Test
    public void testFreestyle() throws IOException {
        String jobName = "favoritescards-freestyle";
        FreestyleJob freestyle = freestyleFactory.pipeline(FOLDER, jobName).create("echo hello\nsleep 5\necho world");
        String fullName = freestyle.getFullName();

        dashboardPage.open();
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(1);
        dashboardPage.clickFavoriteCardRunButton(fullName);
        dashboardPage.checkFavoriteCardStatus(fullName, RUNNING, SUCCESS);
        dashboardPage.removeFavoriteCard(fullName);
        dashboardPage.checkFavoriteCardCount(0);
        dashboardPage.checkIsPipelineListItemFavorited(jobName, false);
    }

    @Test
    public void testClassicPipeline() throws IOException {
        String jobName = "favoritescards-pipeline";
        String script = resources.loadJenkinsFile();
        ClassicPipeline pipeline = pipelineFactory.pipeline(FOLDER, jobName).createPipeline(script).build();
        String fullName = pipeline.getFullName();

        dashboardPage.open();
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(1);

        dashboardPage.checkFavoriteCardStatus(fullName, SUCCESS);
        dashboardPage.clickFavoriteCardRunButton(fullName);
        dashboardPage.checkFavoriteCardStatus(fullName, RUNNING, SUCCESS);
        dashboardPage.clickFavoriteCardReplayButton(fullName);
        dashboardPage.checkFavoriteCardStatus(fullName, RUNNING, SUCCESS);
        dashboardPage.removeFavoriteCard(fullName);
        dashboardPage.checkFavoriteCardCount(0);
        dashboardPage.checkIsPipelineListItemFavorited(jobName, false);
    }

    @Test
    public void testMultibranch() throws IOException, GitAPIException {
        String branchOther = "feature/1";

        git.writeJenkinsFile(resources.loadJenkinsFile());
        git.addAll();
        git.commit("First");
        git.createBranch(branchOther);

        String jobName = "navigation-multibranch";
        MultiBranchPipeline pipeline = multibranchFactory.pipeline(FOLDER, jobName).createPipeline(git);
        String fullNameMaster = pipeline.getFullName();
        String fullNameOther = pipeline.getFullName(branchOther);

        dashboardPage.open();
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.getFavoriteCard(fullNameMaster);
        dashboardPage.clickFavoriteCardActivityLink(fullNameMaster);

        activityPageFactory
            .withPipeline(pipeline)
            .clickBranchTab()
            .toggleFavoriteStatus(branchOther);
        go(-2);

        List<String> cardFullnames = ImmutableList.of(fullNameMaster, fullNameOther);
        int count = 2;

        dashboardPage.checkFavoriteCardStatus(fullNameMaster, SUCCESS);
        dashboardPage.checkFavoriteCardStatus(fullNameOther, SUCCESS);

        for (String fullName : cardFullnames) {
            logger.info(String.format("running tests against favorited branch: %s", fullName));
            count--;
            dashboardPage.clickFavoriteCardRunButton(fullName);
            dashboardPage.checkFavoriteCardStatus(fullName, RUNNING, SUCCESS);
            dashboardPage.clickFavoriteCardReplayButton(fullName);
            dashboardPage.checkFavoriteCardStatus(fullName, RUNNING, SUCCESS);
            dashboardPage.removeFavoriteCard(fullName);
            dashboardPage.checkFavoriteCardCount(count);
            dashboardPage.checkIsPipelineListItemFavorited(jobName, false);
        }
    }

}

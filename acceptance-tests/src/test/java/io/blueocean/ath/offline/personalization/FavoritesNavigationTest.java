package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BaseUrl;
import io.blueocean.ath.Login;
import io.blueocean.ath.ResourceResolver;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.api.classic.ClassicJobApi;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.sse.SSEClientRule;
import io.blueocean.ath.sse.SSEEvents;
import io.jenkins.blueocean.util.HttpRequest;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author cliffmeyers
 */
@Login
@RunWith(ATHJUnitRunner.class)
public class FavoritesNavigationTest implements WebDriverMixin {
    private static final Logger logger = Logger.getLogger(FavoritesNavigationTest.class);
    private static final Folder FOLDER = new Folder("personalization-folder");

    @Inject @Rule
    public SSEClientRule sseClient;

    @Inject
    FavoritesDashboardPage dashboardPage;

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    RunDetailsPipelinePageFactory runDetailsPageFactory;

    @Inject
    ClassicJobApi jobApi;

    @Inject
    FreestyleJobFactory freestyleFactory;

    @Inject
    ClassicPipelineFactory pipelineFactory;

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
        String jobName = "navigation-freestyle";
        FreestyleJob freestyle = freestyleFactory.pipeline(FOLDER, jobName).create("echo hi");
        String fullName = freestyle.getFullName();
        // build and wait for completion so that "run details" link works
        freestyle.build();
        sseClient.untilEvents(SSEEvents.activityComplete(fullName));

        addAsFavorite(freestyle);
        checkRunDetails(freestyle);
        back();
        checkActivity(freestyle)
            .toggleFavorite();
        back();
        dashboardPage.checkFavoriteCardCount(0);
    }

    @Test
    public void testClassic() throws IOException {
        String jobName = "navigation-classic";
        String script = resources.loadJenkinsFile();
        ClassicPipeline pipeline = pipelineFactory.pipeline(FOLDER, jobName).createPipeline(script).build();
        String fullName = pipeline.getFullName();
        // build and wait for completion so that "run details" link works
        pipeline.build();
        sseClient.untilEvents(SSEEvents.activityComplete(fullName));

        addAsFavorite(pipeline);
        checkRunDetails(pipeline);
        back();
        checkActivity(pipeline)
            .toggleFavorite();
        back();
        dashboardPage.checkFavoriteCardCount(0);
    }

    /**
     * Add pipeline as favorite via dashboard and ensure state is correct
     * @param pipeline
     */
    private void addAsFavorite(AbstractPipeline pipeline) {
        dashboardPage.open();
        dashboardPage.togglePipelineListFavorite(pipeline.getName());
        dashboardPage.checkFavoriteCardCount(1);
    }

    /**
     * Check that navigating to run details via favorites card works
     * @param pipeline
     */
    private void checkRunDetails(AbstractPipeline pipeline) {
        dashboardPage.clickFavoriteCardRunDetailsLink(pipeline.getFullName());
        runDetailsPageFactory
            .withPipeline(pipeline)
            .checkBasicDomElements();
    }

    /**
     * Check that navigating to run details via favorites card works
     * @param pipeline
     */
    private ActivityPage checkActivity(AbstractPipeline pipeline) {
        dashboardPage.clickFavoriteCardActivityLink(pipeline.getFullName());
        ActivityPage activityPage = activityPageFactory.withPipeline(pipeline);
        activityPage.checkBasicDomElements();
        activityPage.checkFavoriteStatus(true);
        return activityPage;
    }

}

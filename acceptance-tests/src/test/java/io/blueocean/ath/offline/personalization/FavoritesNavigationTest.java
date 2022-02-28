package io.blueocean.ath.offline.personalization;

import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.factory.RunDetailsPipelinePageFactory;
import io.blueocean.ath.model.AbstractPipeline;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.pages.blue.ActivityPage;
import io.blueocean.ath.pages.blue.BranchPage;
import io.blueocean.ath.sse.SSEClientRule;
import io.blueocean.ath.sse.SSEEvents;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author cliffmeyers
 */
public class FavoritesNavigationTest extends AbstractFavoritesTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FavoritesNavigationTest.class);

    @Inject @Rule
    public SSEClientRule sseClient;

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    RunDetailsPipelinePageFactory runDetailsPageFactory;

    @Override
    protected Logger getLogger() {
        return LOGGER;
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

    @Test
    public void testMultibranch() throws IOException, GitAPIException {
        String branchMaster = "master";
        String branchOther = "feature/1";

        git.writeJenkinsFile(resources.loadJenkinsFile());
        git.addAll();
        git.commit("First");
        git.createBranch(branchOther);

        String jobName = "navigation-multibranch";
        MultiBranchPipeline pipeline = multibranchFactory.pipeline(FOLDER, jobName).createPipeline(git);
        String fullName = FOLDER.getPath() + "/" + jobName;
        sseClient.untilEvents(SSEEvents.activityComplete(fullName));

        // the basics
        addAsFavorite(pipeline);
        checkRunDetails(pipeline);
        back();
        checkActivity(pipeline);
        dashboardPage.open();

        // check the branches tab
        BranchPage branches = navigateBranches(pipeline)
            .checkFavoriteStatus(branchMaster, true)
            .checkFavoriteStatus(branchOther, false)
            .toggleFavoriteStatus(branchOther);

        // test linking to run details
        branches
            .openRunDetails(branchMaster)
            .checkBasicDomElements()
            .back();

        branches
            .openRunDetails(branchOther)
            .checkBasicDomElements()
            .back();

        // check dashboard favorites
        go(-2);
        dashboardPage.checkFavoriteCardCount(2);
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

    private BranchPage navigateBranches(MultiBranchPipeline pipeline) {
        dashboardPage.clickFavoriteCardActivityLink(pipeline.getFullName());
        return activityPageFactory.withPipeline(pipeline).clickBranchTab();
    }

}

package io.blueocean.ath.offline.personalization;


import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.sse.SSEClientRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static io.blueocean.ath.model.BlueJobStatus.RUNNING;
import static io.blueocean.ath.model.BlueJobStatus.SUCCESS;

/**
 * @author cliffmeyers
 */
public class FavoritesCardsTest extends AbstractFavoritesTest {
    private static final Logger logger = LoggerFactory.getLogger(FavoritesAddRemoveTest.class);
    private static final Folder FOLDER = new Folder("personalization-folder");

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Inject
    ActivityPageFactory activityPageFactory;

    @Inject
    WebDriver driver;

    @Inject
    WaitUtil wait;

    @Rule
    @Inject
    public SSEClientRule sseClientRule;

    @After
    public void clearEvents() {
        sseClientRule.clear();
    }

    @Test
    public void testFreestyle() throws IOException {
        File tmpFile = File.createTempFile(UUID.randomUUID().toString(), "");
        String tmpFileName = tmpFile.getAbsolutePath();
        tmpFile.delete();

        String jobName = "favoritescards-freestyle";
        FreestyleJob freestyle = freestyleFactory.pipeline(FOLDER, jobName).create("echo hello\n" +
                "while [ ! -f "+ tmpFileName +" ]\n" +
                        "do\n" +
                        "  sleep 1\n" +
                        "done\n" +
                "\necho world");
        String fullName = freestyle.getFullName();
        dashboardPage.open();

        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(1);
        dashboardPage.clickFavoriteCardRunButton(fullName);
        dashboardPage.checkFavoriteCardStatus(fullName, RUNNING);
        tmpFile.createNewFile();

        sseClientRule.untilEvents(freestyle.buildsFinished);
        sseClientRule.clear();
        dashboardPage.checkFavoriteCardStatus(fullName, SUCCESS);
        dashboardPage.removeFavoriteCard(fullName);
        dashboardPage.checkFavoriteCardCount(0);
        dashboardPage.checkIsPipelineListItemFavorited(jobName, false);
    }

    @Test
    public void testClassicPipeline() throws IOException {
        File tmpFile = File.createTempFile(UUID.randomUUID().toString(), "");
        String tmpFileName = tmpFile.getAbsolutePath();
        tmpFile.delete();

        dashboardPage.open();

        String jobName = "favoritescards-pipeline";
        String script = resources.loadJenkinsFile().replaceAll("%TMPFILENAME%", tmpFileName);
        ClassicPipeline pipeline = pipelineFactory.pipeline(FOLDER, jobName).createPipeline(script);

        String fullName = pipeline.getFullName();
        dashboardPage.togglePipelineListFavorite(jobName);
        dashboardPage.checkFavoriteCardCount(1);
        dashboardPage.clickFavoriteCardRunButton(fullName);
        dashboardPage.checkFavoriteCardStatus(fullName, RUNNING);
        tmpFile.createNewFile();
        sseClientRule.untilEvents(pipeline.buildsFinished);
        sseClientRule.clear();
        dashboardPage.checkFavoriteCardStatus(fullName, SUCCESS);

        tmpFile.delete();
        dashboardPage.clickFavoriteCardReplayButton(fullName);
        dashboardPage.checkFavoriteCardStatus(fullName, RUNNING);
        tmpFile.createNewFile();
        sseClientRule.untilEvents(pipeline.buildsFinished);
        sseClientRule.clear();
        dashboardPage.checkFavoriteCardStatus(fullName, SUCCESS);
        dashboardPage.removeFavoriteCard(fullName);
        dashboardPage.checkFavoriteCardCount(0);
        dashboardPage.checkIsPipelineListItemFavorited(jobName, false);
    }

    public ExpectedCondition<Boolean> hoverBackgroundColor () {
        return (driver) -> {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object backgroundColor = js.executeScript(
                "return getComputedStyle(document.querySelectorAll('.actions-container')[0], ':after').getPropertyValue('background-color')"
            );

            if (backgroundColor.toString().equals("rgba(0, 0, 0, 0)")) {
                return true;
            } else {
                return false;
            }
        };
    }
}

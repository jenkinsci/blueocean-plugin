package io.blueocean.ath.offline.personalization;

import com.google.common.collect.ImmutableList;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.factory.ActivityPageFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.model.Folder;
import io.blueocean.ath.model.FreestyleJob;
import io.blueocean.ath.model.MultiBranchPipeline;
import io.blueocean.ath.sse.SSEClientRule;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.blueocean.ath.model.BlueJobStatus.RUNNING;
import static io.blueocean.ath.model.BlueJobStatus.SUCCESS;

/**
 * @author cliffmeyers
 */
public class FavoritesCardsTest extends AbstractFavoritesTest {
    private static final Logger logger = Logger.getLogger(FavoritesAddRemoveTest.class);
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
        dashboardPage.checkFavoriteCardStatus(fullName, SUCCESS);

        tmpFile.delete();
        dashboardPage.clickFavoriteCardReplayButton(fullName);
        dashboardPage.checkFavoriteCardStatus(fullName, RUNNING);
        tmpFile.createNewFile();
        sseClientRule.untilEvents(pipeline.buildsFinished);
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

    @Test
    public void testMultibranch() throws IOException, GitAPIException, InterruptedException {
        File tmpFile = File.createTempFile(UUID.randomUUID().toString(), "");
        String tmpFileName = tmpFile.getAbsolutePath();
        tmpFile.delete();

        String branchOther = "feature/1";

        git.writeJenkinsFile(resources.loadJenkinsFile().replaceAll("%TMPFILENAME%", tmpFileName));
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
        dashboardPage.open();

        List<String> cardFullnames = ImmutableList.of(fullNameMaster, fullNameOther);
        int count = 2;

        tmpFile.createNewFile();
        sseClientRule.untilEvents(pipeline.buildsFinished);

        dashboardPage.open(); // FIXME - because sse is not yet registered (started before the page was loaded)

        dashboardPage.checkFavoriteCardStatus(fullNameMaster, SUCCESS);
        dashboardPage.checkFavoriteCardStatus(fullNameOther, SUCCESS);

        //Check that favorite actions are hidden until pointer is hovering the row
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object backgroundColor = js.executeScript(
            "return getComputedStyle(document.querySelectorAll('.actions-container')[0], ':after').getPropertyValue('background-color')"
        );

        //check that background color of overlay is  NOT transparent when pointer is NOT hovering the favorite row
        Assert.assertNotEquals(backgroundColor, "rgba(0, 0, 0, 0)");

        logger.info("move pointer over favorite card actions");

        Actions action = new Actions(driver);
        WebElement we = wait.until(By.xpath("//*[@class=\"pipeline-card success-bg-lite\"]"));
        action.moveToElement(we).perform();

        //check that background color of overlay is transparent when pointer is hovering the favorite row
        wait.until(hoverBackgroundColor());

        tmpFile.delete();
        for (String fullName : cardFullnames) {
            logger.info(String.format("running tests against favorited branch: %s", fullName));
            count--;
            dashboardPage.clickFavoriteCardRunButton(fullName);
            dashboardPage.checkFavoriteCardStatus(fullName, RUNNING);
            tmpFile.createNewFile();
            sseClientRule.untilEvents(pipeline.buildsFinished);
            dashboardPage.checkFavoriteCardStatus(fullName, SUCCESS);
            tmpFile.delete();

            dashboardPage.clickFavoriteCardReplayButton(fullName);
            dashboardPage.checkFavoriteCardStatus(fullName, RUNNING);
            tmpFile.createNewFile();
            sseClientRule.untilEvents(pipeline.buildsFinished);
            dashboardPage.checkFavoriteCardStatus(fullName, SUCCESS);
            tmpFile.delete();
            dashboardPage.removeFavoriteCard(fullName);
            dashboardPage.checkFavoriteCardCount(count);
            dashboardPage.checkIsPipelineListItemFavorited(jobName, false);
        }
    }

}

package io.blueocean.ath.pages.blue;

import com.google.common.base.Strings;
import io.blueocean.ath.WaitUtil;
import io.blueocean.ath.model.MultiBranchPipeline;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.inject.Inject;

public class EditorPage {
    private Logger logger = Logger.getLogger(EditorPage.class);

    private MultiBranchPipeline pipeline;

    private WebDriver driver;

    @Inject
    private WaitUtil wait;

    @Inject
    public EditorPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void simplePipeline() {
        simplePipeline(null);
    }

    public void saveBranch(String branch) {
        logger.info("Editing pipeline - saving now");
        wait.until(By.xpath("//*[text()='Save']")).click();
        wait.until(By.cssSelector("textarea[placeholder=\"What changed?\"]")).sendKeys("We changed some things.");
        if(!Strings.isNullOrEmpty(branch)) {
            wait.until(By.xpath("//span[text()='Commit to new branch']")).click();
            wait.until(By.cssSelector("input[placeholder='my-new-branch']:enabled")).sendKeys(branch);
            logger.info("Testing removal of spaces in branch name");
            wait.until(ExpectedConditions.textToBePresentInElementValue(By.cssSelector("input[placeholder='my-new-branch']:enabled"), branch.replaceAll("\\s","")));
            logger.info("Using branch " + branch.replaceAll("\\s",""));
        } else {
            logger.info("Using branch master");
        }
        wait.until(By.xpath("//*[text()=\"Save & run\"]")).click();

        logger.info("Saved new branch");
    }
    public void simplePipeline(String newBranch) {
        logger.info("Editing simple pipeline");
        wait.until(By.xpath("(//*[@class='pipeline-node-hittarget'])[2]")).click();
        wait.until(By.cssSelector("input.stage-name-edit")).sendKeys("Test stage");
        wait.until(By.cssSelector("button.btn-primary.add")).click();
        wait.until(By.xpath("//*[text()='Print Message']")).click();
        wait.until(By.cssSelector("input.TextInput-control")).sendKeys("hi there");
        wait.click(By.xpath("(//a[@class='back-from-sheet'])[2]"));
        wait.until(By.xpath("//*[text()='Save']")).click();
        wait.until(By.cssSelector("textarea[placeholder=\"What changed?\"]")).sendKeys("Simple pipeline");
        if(!Strings.isNullOrEmpty(newBranch)) {
            wait.until(By.xpath("//*[text()='Commit to new branch']")).click();
            wait.until(By.cssSelector("input[placeholder='my-new-branch']:enabled")).sendKeys(newBranch);
            logger.info("Using branch " + newBranch);
        } else {
            logger.info("Using branch master");
        }
        wait.until(By.xpath("//*[text()=\"Save & run\"]")).click();
        logger.info("Simple pipeline saved");
    }

    // Creates a pipeline with parallel stages in it.
    public void parallelPipeline(String newBranch, int numberOfParallels) {
        logger.info("Editing a parallel pipeline");
        /*
        We'll create as many parallel stages as we were told to
        via int numberOfParallels when we were called.
        */
        for (int i = 0; i < numberOfParallels; i++) {
            logger.info("Create stage Parallel-" + i);
            wait.click(By.xpath("(//*[@class='pipeline-node-hittarget'])[2]"));
            wait.until(By.cssSelector("input.stage-name-edit")).sendKeys("Parallel-" + i);
            wait.click(By.cssSelector("button.btn-primary.add"));
            wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"sh\"]"));
            wait.until(By.cssSelector("textarea.editor-step-detail-script")).sendKeys("netstat -a");
            wait.click(By.xpath("(//a[@class='back-from-sheet'])[2]"));
        }
        /*
        Now we need to name the "wrapper" stage to something other than what
        got automatically put in.
        */
        wait.click(By.cssSelector("div.pipeline-big-label.top-level-parallel"));
        wait.until(By.cssSelector("input.stage-name-edit")).clear();
        wait.until(By.cssSelector("input.stage-name-edit")).sendKeys("Top Level Parallel Wrapper Stage");
        wait.click(By.cssSelector("button.btn-primary.inverse"));
        wait.until(By.cssSelector("textarea[placeholder=\"What changed?\"]")).sendKeys("Parallel pipeline");
        if(!Strings.isNullOrEmpty(newBranch)) {
            logger.info("Saving to branch " + newBranch);
            wait.click(By.xpath("//*[text()='Commit to new branch']"));
            wait.until(By.cssSelector("input[placeholder='my-new-branch']:enabled")).sendKeys(newBranch);
        } else {
            /*
            This mimics the user changing picking a new branch, and then
            changing their mind and committing to master after all.
            */
            wait.click(By.xpath("//*[text()='Commit to new branch']"));
            wait.until(By.cssSelector("input[placeholder='my-new-branch']:enabled")).sendKeys("i-am-changing-my-mind");
            wait.click(By.xpath("//*[text()='Commit to master']"));
            logger.info("Using branch master");
        }
        wait.click(By.xpath("//*[text()=\"Save & run\"]"));
        logger.info("Save & run clicked, Parallel pipeline saved");
    }

}

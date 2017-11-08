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
        wait.until(By.cssSelector("textarea[placeholder=\"What changed?\"]")).sendKeys("Simple pipeline");
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
            wait.until(By.xpath("//span[@text='Commit to new branch'")).click();
            wait.until(By.cssSelector("input[placeholder='my-new-branch']:enabled")).sendKeys(newBranch);
            logger.info("Using branch " + newBranch);
        } else {
            logger.info("Using branch master");
        }
        wait.until(By.xpath("//*[text()=\"Save & run\"]")).click();
        logger.info("Simple pipeline saved");
    }

    // Creates a parallel pipeline
    public void parallelPipeline(String newBranch) {
        logger.info("Editing a parallel pipeline");
        // Creating first parallel stage
        logger.info("Create our first parallel stage");
        wait.until(By.xpath("(//*[@class='pipeline-node-hittarget'])[2]")).click();
        wait.until(By.cssSelector("input.stage-name-edit")).sendKeys("Parallel-1");
        wait.until(By.cssSelector("button.btn-primary.add")).click();
        wait.until(By.xpath("//*[text()='Shell Script']")).click();
        wait.until(By.cssSelector("textarea.editor-step-detail-script")).sendKeys("netstat -a");
        wait.click(By.xpath("(//a[@class='back-from-sheet'])[2]"));
        // Now let's create the next one.
        logger.info("Create our second parallel stage");
        wait.until(By.xpath("(//*[@class='pipeline-node-hittarget'])[2]")).click();
        logger.info("\n--> KS NAME PARALLEL 2 --> click cssSelector input.stage-name-edit\n");
        wait.until(By.cssSelector("input.stage-name-edit")).sendKeys("Parallel-2");
        wait.until(By.cssSelector("button.btn-primary.add")).click();
        wait.until(By.xpath("//*[text()='Shell Script']")).click();
        wait.until(By.cssSelector("textarea.editor-step-detail-script")).sendKeys("whoami");
        wait.click(By.xpath("(//a[@class='back-from-sheet'])[2]"));

        // Here's where we save the pipeline. This comes later.
        wait.until(By.xpath("//*[text()='Save']")).click();
        wait.until(By.cssSelector("textarea[placeholder=\"What changed?\"]")).sendKeys("Parallel pipeline");
        if(!Strings.isNullOrEmpty(newBranch)) {
            // This isn't working. It's not finding the Commit to new branch thing to click on.
            wait.until(By.xpath("//span[@text='Commit to new branch'")).click();
            wait.until(By.cssSelector("input[placeholder='my-new-branch']:enabled")).sendKeys(newBranch);
            logger.info("Using branch " + newBranch);
        } else {
            // Maybe we should click on the Commit to new branch button first, then
            // have the automation 'change its mind' so to speak, just to make
            // sure that works.

            logger.info("Using branch master");
        }
        wait.until(By.xpath("//*[text()=\"Save & run\"]")).click();
        logger.info("Simple pipeline saved");
    }

}

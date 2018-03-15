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

    /**
     * Creates a simple pipeline, then re-edits it to add a stage
     * with two steps in it.
     *
     * @param pipelineToEdit the AbstractPipeline object we're working with
     * @param newBranch the name of the new branch we'll save to
     * @param newStageName the name of the new stage we'll create
     */
    public void addStageToPipeline(MultiBranchPipeline pipelineToEdit, String newBranch, String newStageName) {
        pipeline = pipelineToEdit;
        logger.info("Editing pipeline " + pipeline.getName() + ", saving to branch " + newBranch + ", with new stage " + newStageName);
        wait.click(By.id("pipeline-node-hittarget-3-add"));
        wait.sendKeys(By.cssSelector("input.stage-name-edit"),newStageName);
        logger.info("Adding a shell step");
        wait.click(By.cssSelector("button.btn-primary.add"));
        wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"sh\"]"));
        wait.sendKeys(By.cssSelector("textarea.editor-step-detail-script"),"whoami");
        // This selector makes sure we always click on the back arrow in the active sheet.
        logger.info("Clicking on active sheet div.sheet.active a.back-from-sheet");
        wait.click(By.cssSelector("div.sheet.active a.back-from-sheet"));
        logger.info("Adding an echo step");
        wait.click(By.cssSelector("button.btn-primary.add"));
        wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"echo\"]"));
        wait.sendKeys(By.cssSelector("input.TextInput-control"),"Echo step added by ATH");
        // Same thing here - we know we'll click on the active sheet now.
        logger.info("Clicking on active sheet div.sheet.active a.back-from-sheet");
        wait.click(By.cssSelector("div.sheet.active a.back-from-sheet"));
        logger.info("Stages added, about to save");
        wait.click(By.xpath("//*[text()='Save']"));
        wait.sendKeys(By.cssSelector("textarea[placeholder=\"What changed?\"]"),"We added a stage");
        if(!Strings.isNullOrEmpty(newBranch)) {
            wait.click(By.xpath("//*[text()='Commit to new branch']"));
            wait.sendKeys(By.cssSelector("input[placeholder='my-new-branch']:enabled"),newBranch);
            logger.info("Using branch " + newBranch);
        } else {
            logger.info("Using branch master");
        }
        wait.click(By.xpath("//*[text()=\"Save & run\"]"));
        logger.info("Pipeline saved with edited stages in place");
    }

    /**
     * Saves the pipeline to a branch
     *
     * @param branch the name of the new branch we'll save to. If null,
     *               we save to master.
     */
    public void saveBranch(String branch) {
        logger.info("Editing pipeline - saving now");
        wait.click(By.xpath("//*[text()='Save']"));
        wait.sendKeys(By.cssSelector("textarea[placeholder=\"What changed?\"]"), "We changed some things.");
        if(!Strings.isNullOrEmpty(branch)) {
            wait.click(By.xpath("//span[text()='Commit to new branch']"));
            wait.sendKeys(By.cssSelector("input[placeholder='my-new-branch']:enabled"),branch);
            logger.info("Testing removal of spaces in branch name");
            wait.until(ExpectedConditions.textToBePresentInElementValue(By.cssSelector("input[placeholder='my-new-branch']:enabled"), branch.replaceAll("\\s","")));
            logger.info("Using branch " + branch.replaceAll("\\s",""));
        } else {
            logger.info("Using branch master");
        }
        wait.click(By.xpath("//*[text()=\"Save & run\"]"));
        logger.info("Saved new branch");
    }

    /**
     * Creates a simple pipeline from scratch.
     *
     * @param newBranch the name of the new branch we'll save to. If null,
     *                  we save to master.
     */
    public void simplePipeline(String newBranch) {
        logger.info("Creating and editing simple pipeline");
        logger.info("Adding a stage by clicking on id pipeline-node-hittarget-2-add");
        wait.click(By.id("pipeline-node-hittarget-2-add"));
        logger.info("simplePipeline naming the stage");
        wait.sendKeys(By.cssSelector("input.stage-name-edit"),"simplePipeline creating Test stage");
        logger.info("simplePipeline clicking the Add Step button");
        wait.click(By.cssSelector("button.btn-primary.add"));
        // Let's change this to a cssSelector:
        // wait.click(By.xpath("//*[text()='Print Message']"));
        logger.info("simplePipeline selecting an echo step from the dropdown");
        wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"echo\"]"));
        logger.info("simplePipeline entering the echo message");
        wait.sendKeys(By.cssSelector("input.TextInput-control"),"simplePipeline creating echo message");
        // Newly available cssSelector that targets the back-from-sheet which is on the active sheet.
        logger.info("simplePipeline committing that change by clicking on active sheet div.sheet.active a.back-from-sheet");
        wait.click(By.cssSelector("div.sheet.active a.back-from-sheet"));
        logger.info("simplePipeline clicking the Save button");
        wait.click(By.xpath("//*[text()='Save']"));
        logger.info("simplePipeline providing a commit message");
        wait.sendKeys(By.cssSelector("textarea[placeholder=\"What changed?\"]"),"We changed some things via ATH");
        if(!Strings.isNullOrEmpty(newBranch)) {
            logger.info("simplePipeline Choosing to commit to new branch");
            wait.click(By.xpath("//*[text()='Commit to new branch']"));
            logger.info("simplePipeline setting the branch name to " + newBranch);
            wait.sendKeys(By.cssSelector("input[placeholder='my-new-branch']:enabled"),newBranch);
            // logger.info("Using branch " + newBranch);
        } else {
            logger.info("Using branch master");
        }
        wait.click(By.xpath("//*[text()=\"Save & run\"]"));
        logger.info("Simple pipeline saved");
    }

    /**
     * Creates a parallel pipeline from scratch.
     *
     * @param newBranch the name of the new branch we'll save to. If null,
     *                  we save to master.
     */
    public void parallelPipeline(String newBranch, int numberOfParallels) {
        logger.info("Editing a parallel pipeline");
        /*
        We'll create as many parallel stages as we were told to
        via int numberOfParallels when we were called.
        */
        for (int i = 1; i < numberOfParallels; i++) {
            logger.info("Create stage Parallel-" + i);
            logger.info("--> WHAT NODE TO CLICK ON?");
            /*
            We're only creating one stage. So the "add" button will always have
            the id pipeline-node-hittarget-2-add, because it is the second
            column in the "grid," so to speak.
             */
            wait.click(By.id("pipeline-node-hittarget-2-add"));
            wait.sendKeys(By.cssSelector("input.stage-name-edit"),("Parallel-" + i));
            wait.click(By.cssSelector("button.btn-primary.add"));
            wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"sh\"]"));
            wait.sendKeys(By.cssSelector("textarea.editor-step-detail-script"),"netstat -a");
            logger.info("Clicking on active sheet div.sheet.active a.back-from-sheet");
            wait.click(By.cssSelector("div.sheet.active a.back-from-sheet"));

        }
        /*
        Now we need to name the "wrapper" stage to something other than what
        got automatically put in.
        */
        wait.click(By.cssSelector("div.pipeline-big-label.top-level-parallel"));
        wait.clear(By.cssSelector("input.stage-name-edit"));
        wait.sendKeys(By.cssSelector("input.stage-name-edit"),"Top Level Parallel Wrapper Stage");
        wait.click(By.cssSelector("button.btn-primary.inverse"));
        wait.sendKeys(By.cssSelector("textarea[placeholder=\"What changed?\"]"),"Parallel pipeline");
        if(!Strings.isNullOrEmpty(newBranch)) {
            logger.info("Saving to branch " + newBranch);
            wait.click(By.xpath("//*[text()='Commit to new branch']"));
            wait.sendKeys(By.cssSelector("input[placeholder='my-new-branch']:enabled"),newBranch);
        } else {
            /*
            This mimics the user changing picking a new branch, and then
            changing their mind and committing to master after all.
            */
            wait.click(By.xpath("//*[text()='Commit to new branch']"));
            wait.sendKeys(By.cssSelector("input[placeholder='my-new-branch']:enabled"),"i-am-changing-my-mind");
            wait.click(By.xpath("//*[text()='Commit to master']"));
            logger.info("Using branch master");
        }
        wait.click(By.xpath("//*[text()=\"Save & run\"]"));
        logger.info("Save & run clicked, Parallel pipeline saved");
    }

}

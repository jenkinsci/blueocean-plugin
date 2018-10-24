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
     * @param newStageName the name of the new stage we'll create
     */
    public void addStageToPipeline(MultiBranchPipeline pipelineToEdit, String newStageName) {
        pipeline = pipelineToEdit;
        logger.info("Editing pipeline " + pipeline.getName() + "to add a new stage " + newStageName);
        wait.click(By.id("pipeline-node-hittarget-3-add"));
        wait.sendKeys(By.cssSelector("input.stage-name-edit"),newStageName);
        logger.info("Adding a shell step");
        wait.click(By.cssSelector("button.btn-primary.add"));
        wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"sh\"]"));
        wait.sendKeys(By.cssSelector("textarea.editor-step-detail-script"),"whoami");
        // This selector makes sure we always click on the back arrow in the active sheet.
        wait.click(By.cssSelector("div.sheet.active a.back-from-sheet"));
        logger.info("Adding an echo step");
        wait.click(By.cssSelector("button.btn-primary.add"));
        wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"echo\"]"));
        wait.sendKeys(By.cssSelector("input.TextInput-control"),"Echo step added by ATH");
        wait.click(By.cssSelector("div.sheet.active a.back-from-sheet"));
        logger.info("Stage " + newStageName + " added");
    }

    /**
     * Deletes an entire stage of a pipeline.
     *
     * @param stageName the step we want to delete
     *
     */
    public void deleteStage(String stageName) {
        logger.info("Deleting stage " + stageName);
        wait.click(By.xpath("//*[@data-stagename=\"" + stageName + "\"]"));
        // Click the little popup button
        wait.click(By.cssSelector("div.more-menu"));
        // Click Delete
        // TODO: Ongoing work to get rid of text-based selectors. This is one.
        wait.click(By.xpath("//*[text()=\"Delete\"]"));
        logger.info("Successfully deleted stage " + stageName);
    }

    /**
     * Changes the agent label from one thing to something else.
     *
     * @param newAgentLabel the name of the new agent we'll change to
     *
     * TODO: Needs the ability to select something like `docker` and be
     *       able to add strings to the resulting popup
     */
    public void setAgentLabel(String newAgentLabel) {
        logger.info("Changing agent label to " + newAgentLabel);
        // Click the start node of the pipeline, in case it wasn't already
        wait.click(By.id("pipeline-node-hittarget-1-start"));
        // Click the agent dropdown to display the list
        wait.click(By.cssSelector("button.Dropdown-button"));
        wait.click(By.xpath("//*[text()='" + newAgentLabel + "']"));
        logger.info("Agent label changed to " + newAgentLabel);
    }

    /**
     * Sets a new environment variable for a pipeline. This
     * sets a pipeline-wide variable, not a per-stage variable.
     *
     * @param envVarKey the the new environment variable
     * @param envVarValue the value for the environment
     *
     */
    public void setEnvironmentVariable(String envVarKey, String envVarValue) {
        logger.info("Setting a new variable " + envVarKey + " to " + envVarValue);
        // Click the button which indicates the start of our pipeline
        wait.click(By.id("pipeline-node-hittarget-1-start"));
        // Click the button to bring up the environment variable input boxes
        wait.click(By.cssSelector("button.environment-add-delete-icon.add"));
        wait.sendKeys(By.cssSelector("div.TextInput.env-key input.TextInput-control"),envVarKey);
        wait.sendKeys(By.cssSelector("div.TextInput.env-value input.TextInput-control"),envVarValue);
        logger.info("Environment variable " + envVarKey + " now set to " + envVarValue);
    }

    /**
     * Saves the pipeline to a branch
     *
     * @param branch the name of the new branch we'll save to. If null,
     *               we save to master.
     */
    public void saveBranch(String branch) {
        logger.info("saveBranch method called");
        wait.click(By.xpath("//*[text()='Save']"));
        // In some CI infra, we're getting failures probably half the time. Let's
        // sleep here as a safeguard.
        wait.tinySleep(1000);
        logger.info("wait.until the What Changed textarea appears");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea[placeholder=\"What changed?\"]")));
        logger.info("Entering commit message into What Changed textarea");
        wait.sendKeys(By.cssSelector("textarea[placeholder=\"What changed?\"]"), "ATH made changes and is saving");
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
        logger.info("Saved branch");
    }

    /**
     * Creates a simple pipeline from scratch, and saves it as the
     * branch specified when called.
     *
     * @param newBranch the name of the new branch we'll save to. If null,
     *                  we save to master.
     */
    public void simplePipeline(String newBranch) {
        logger.info("Creating and editing simple pipeline");
        wait.click(By.id("pipeline-node-hittarget-2-add"));
        wait.sendKeys(By.cssSelector("input.stage-name-edit"),"simplePipeline creating Test stage");
        wait.click(By.cssSelector("button.btn-primary.add"));
        logger.info("Adding an echo step");
        wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"echo\"]"));
        wait.sendKeys(By.cssSelector("input.TextInput-control"),"simplePipeline creating echo message");
        wait.click(By.cssSelector("div.sheet.active a.back-from-sheet"));
        logger.info("Pipeline created and ready to be saved");
    }

    /**
     * Creates a parallel pipeline from scratch.
     *
     * @param numberOfParallels number of parallel branches we want to create.
     */
    public void parallelPipeline(int numberOfParallels) {
        logger.info("Editing a parallel pipeline");
        for (int i = 1; i < numberOfParallels; i++) {
            logger.info("Create stage Parallel-" + i);
            // The "add" Button will always have the id pipeline-node-hittarget-2-add
            // since it's the second column in the grid.
            wait.click(By.id("pipeline-node-hittarget-2-add"));
            wait.sendKeys(By.cssSelector("input.stage-name-edit"),("Parallel-" + i));
            wait.click(By.cssSelector("button.btn-primary.add"));
            wait.click(By.cssSelector(".editor-step-selector div[data-functionName=\"sh\"]"));
            wait.sendKeys(By.cssSelector("textarea.editor-step-detail-script"),"whoami");
            logger.info("Clicking on active sheet div.sheet.active a.back-from-sheet");
            wait.click(By.cssSelector("div.sheet.active a.back-from-sheet"));
            logger.info("Stage Parallel-" + i + " created");
        }
        // Rename the "wrapper" stage to something non-default
        wait.click(By.cssSelector("div.pipeline-big-label.top-level-parallel"));
        wait.clear(By.cssSelector("input.stage-name-edit"));
        wait.sendKeys(By.cssSelector("input.stage-name-edit"),"Top Level Parallel Wrapper Stage");
        logger.info("Parallel pipeline created and ready to save");
    }
}

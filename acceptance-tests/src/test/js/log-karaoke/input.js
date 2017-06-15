const jobName = 'inputPause';
/** @module input
 * @memberof karaoke
 * @description TEST: validate that the pause for user input is working as it
 * should.
 */
module.exports = {
    /** Create Pipeline Job "inputPause" */
    'Step 01': function (browser) {
        const pipelinesCreate = browser.page.pipelineCreate().navigate();
        pipelinesCreate.createPipeline(jobName, 'pause.groovy');
    },
    /** Build Pipeline Job*/
    'Step 02': function (browser) {
        const pipelinePage = browser.page.jobUtils().forJob(jobName);
        pipelinePage.buildStarted(function() {
            // Reload the job page and check that there was a build done.
            pipelinePage
                .forRun(1)
                .waitForElementVisible('@executer');
        });
    },
    /** Check Job Blue Ocean Pipeline Activity Page has run */
    'Step 03': function (browser) {
        const blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        // Check the run is turning to pause
        blueActivityPage.waitForJobRunPaused(jobName, function () {
            blueActivityPage.waitForRunPausedVisible(jobName, '1');
        });
    },
    /** Check Job Blue Ocean Pipeline Activity Page has run  - then go to the detail page and validate the input form
     * */
    'Step 04': function (browser) {
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(jobName, 'jenkins', 1);
        // test the input parameters
        blueRunDetailPage.validateSupportedInputTypes();
        // submit the form
        blueRunDetailPage.click('@inputStepSubmit');
        // Check the run is turning to unpaused
        blueRunDetailPage.waitForJobRunUnpaused(jobName, function () {
            // wait for job to finish
            blueRunDetailPage.waitForJobRunEnded(jobName);
        });
    }
};

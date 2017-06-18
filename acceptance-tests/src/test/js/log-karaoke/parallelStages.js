const jobName = 'parallelStages';
/** @module stages
 * @memberof karaoke
 * @description REGRESSION-TEST: parallel karaoke not allowing branch selection or completing correctly
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-37962|JENKINS-37962}
 *
 * TODO @see {@link https://issues.jenkins-ci.org/browse/JENKINS-37753|JENKINS-37753}
 * REGRESSION: Steps showing up as incomplete when they are in fact complete
 *
 */
module.exports = {
    /** Create Pipeline Job "parallelStages" */
    'Step 01': function (browser) {
        const pipelinesCreate = browser.page.pipelineCreate().navigate();
        pipelinesCreate.createPipeline(jobName, 'parallel-stages.groovy');
    },
    /** Build Pipeline Job*/
    'Step 02': function (browser) {
        const pipelinePage = browser.page.jobUtils().forJob(jobName);
        pipelinePage.buildStarted(function () {
            // Reload the job page and check that there was a build done.
            pipelinePage
                .forRun(1)
                .waitForElementVisible('@executer');
        });
    },
    /** Check Job Blue Ocean Pipeline Activity Page has run.
     * Check different nodes of the graph, first check that we are in first branch and that we have steps.
     * Then change to the second parallel tree and check the same as before for the secondBranch*/
    'Step 03': function (browser) {
        const blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        // Check the run itself
        blueActivityPage.waitForRunRunningVisible('parallelStages', '1');
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(jobName, 'jenkins', 1);
        blueRunDetailPage.validateGraph();
        // if we have the first stage finished we can go on
        blueRunDetailPage.waitForElementPresent('@circleSuccess');
        // see whether we have focus on the first branch
        blueRunDetailPage.assertLogTitle('firstBranch');
        // give some time by waiting on 2 steps showing up
        blueRunDetailPage.validateSteps(2);
        // navigate to the secondBranch
        blueRunDetailPage.forNode(11); // -> IF groovy changes this might to be adopted
        // see whether we have focus on the second branch
        blueRunDetailPage.assertLogTitle('secondBranch');
        // we should have now 2 steps
        blueRunDetailPage.validateSteps(2);
    },
    /** Wait for job to end, TODO: then validate that the steps are all marked as finished
     * TODO @see {@link https://issues.jenkins-ci.org/browse/JENKINS-37753|JENKINS-37753}
     * */
    'Step 04': function (browser) {
        browser.waitForJobRunEnded(jobName, function () {
            /*
             * Here we will test for JENKINS-37753  -> IF groovy changes this might to be adopted
             */
            const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(jobName, 'jenkins', 1);
            // we should be on the last stage of the pipeline by now
            blueRunDetailPage.validateNotRunningResults();
            // navigate to the first stage in the pipeline
            blueRunDetailPage.forNode(5);
            blueRunDetailPage.validateNotRunningResults();
            // navigate to the first stage in the parallel step in the pipeline
            blueRunDetailPage.forNode(10);
            blueRunDetailPage.validateNotRunningResults();
            // sample taken at random for logs to see whether they are truncated
            blueRunDetailPage.clickFirstResultItem('Running shell script');
            blueRunDetailPage.clickFirstResultItem('secondBranch www.stern.de');
            // navigate to the second stage in the parallel step in the pipeline
            blueRunDetailPage.forNode(11);
            blueRunDetailPage.validateNotRunningResults();

        });
    }
};

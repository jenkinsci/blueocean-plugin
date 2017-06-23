/** @module initialStage
 * @memberof edgeCases
 * @description
 *
 * Tests: test whether the stageGraph is rendered in the first run when the first step is not a stage
 * but afterward we will create stages
 *
 * REGRESSION covered:
 *
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-39229|JENKINS-39229} Initial stage run does not show graph
 *
 *
 */
const jobName = 'initialStage';
module.exports = {
  /** Create Pipeline Job "initialStage" */
  'Step 01': function (browser) {
    const pipelinesCreate = browser.page.pipelineCreate().navigate();
    pipelinesCreate.createPipeline(jobName, jobName + '.groovy');
  },
  /** Build Pipeline Job*/
  'Step 02': function (browser) {
    const pipelinePage = browser.page.jobUtils().forJob(jobName);
    pipelinePage.buildStarted(function () {
      // Reload the job page and check that there was a build started.
      pipelinePage
        .forRun(1)
        .waitForElementVisible('@executer');
    });
  },
  /** Check Job Blue Ocean Pipeline Activity Page has run */
  'Step 03': function (browser) {
    const blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
    // Check the run itself
    blueActivityPage.waitForRunRunningVisible(jobName, '1');
    const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(jobName, 'jenkins', 1);

    blueRunDetailPage.assertBasicLayoutOkay();
    blueRunDetailPage.waitForPipelineStageEvent(jobName, function () {
        blueRunDetailPage.validateGraph(); // test whether we have a pipeline graph
        blueRunDetailPage.waitForJobRunEnded(jobName);
    });
  },
};

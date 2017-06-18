/**
 * @module runDetailsFallbackNavigation
 * @memberof runDetailsModal
 * @description
 *
 * Tests: test whether navigating directly Run Details without specifying a tab allows the close button to work correctly.
 *
 * REGRESSION covered:
 *
 * @see {@link https://issues.jenkins-ci.org/browse/JENKINS-40662|JENKINS-40662} Deep-linking to Run Details
 * screen with no tab specified causes problem when closing modal
 */
const jobName = 'runDetailsFallbackNavigation';
module.exports = {
    'Step 01 - Create Job': function (browser) {
        const pipelinesCreate = browser.page.pipelineCreate().navigate();
        pipelinesCreate.createPipeline(jobName, 'hello-world.groovy');
    },
    'Step 02 - Build Job': function (browser) {
        const pipelinePage = browser.page.jobUtils().forJob(jobName);
        pipelinePage.buildStarted(function () {
            // Reload the job page and check that there was a build done.
            pipelinePage
                .forRun(1)
                .waitForElementVisible('@executer');
        });
    },
    'Step 03 - Open and Close Run Details': function (browser) {
        const blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(jobName, 'jenkins', 1);
        blueRunDetailPage.waitForJobRunEnded(jobName);
        blueRunDetailPage.waitForElementVisible('.BasicHeader--success');
        blueRunDetailPage.closeModal();
        blueRunDetailPage.waitForLocationContains('/activity');
    },
};

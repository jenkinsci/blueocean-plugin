/** @module stages
 * @memberof karaoke
 * @description TEST: logs tailing with stages and steps - karaoke mode
 *
 * Based on three different syntax files we will run the tests against each syntax to make sure they work all as expected.
 * We will cover
 */
// using different syntax of the same pipeline
const useCase = {
  name: 'stagesBlock',
  script: 'stages-with-wait-block-syntax.groovy',
  nodeId: '6',
};

module.exports = {
/** Create Pipeline Job "stages" */
    'Step 01': function (browser) {
        // create the different jobs
        console.log('creating pipeline job', useCase.name, useCase.script);
        // navigate to the create page
        const pipelinesCreate = browser.page.pipelineCreate().navigate();
        pipelinesCreate
            .createPipeline(useCase.name, useCase.script)
        ;
    },
/** Build Pipeline Job */
    'Step 02': function (browser) {
        // we need to create a browser page outside the async loop
        // const pipelinesCreate = browser.page.pipelineCreate().navigate();
        const pipelinePage = browser.page.jobUtils().forJob(useCase.name);
        pipelinePage.buildStarted(function() {
            // Reload the job page and check that there was a build done.
            pipelinePage
                .forRun(1)
                .waitForElementVisible('@executer');
        });
    },
/** Check Job Blue Ocean Pipeline Activity Page has run */
    'Step 03': function (browser) {
        const blueActivityPage = browser.page.bluePipelineActivity().forJob(useCase.name, 'jenkins');
        // Check the run itself
        blueActivityPage.waitForRunRunningVisible(useCase.name, '1');
    },
/** Check Job Blue Ocean Pipeline run detail page - karaoke*/
    'Step 04': function (browser) {
        // this test case tests a live pipeline that is why we only running it with one case
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(useCase.name, 'jenkins', 1);
        blueRunDetailPage.assertBasicLayoutOkay();
        // if we have the first stage finished we are sure in karaoke mode
        blueRunDetailPage.waitForElementPresent('svg circle.success');
        // FIXME should be taken from somewhere dynamically
        // Stop karaoke and go back in graph and see the result
        const nodeDetail = blueRunDetailPage.forNode(useCase.nodeId);
        // validate that karaoke has stopped but overall process still runs
        nodeDetail.waitForElementVisible('g.progress-spinner.running');
        const stepLabelSelector = '.Steps .logConsole:first-child .result-item-label-name';
        // Validate the result of the node
        nodeDetail.waitForElementVisible(stepLabelSelector)
            .getText(stepLabelSelector, function (result) {
                this.assert.equal(result.value.indexOf('Shell Script') >= 0, true);
            })
        ;
        // test whether the expand works
        nodeDetail.clickFirstResultItem();
        // test whether the stage we seeing is highlighted
        nodeDetail.waitForElementVisible('g.pipeline-node-selected');
        // test whether log lines are navigable
        nodeDetail.validateLogConsole(2);
        // wait for job to finish
        nodeDetail.waitForJobRunEnded(useCase.name);
    },
/** Check whether the artifacts tab shows artifacts*/
    'Step 05': function (browser) {
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(useCase.name, 'jenkins', 1);
        blueRunDetailPage.clickTab('artifacts');
        blueRunDetailPage
            .validateNotEmptyArtifacts(1)
            .waitForElementVisible('@artifactTable');
        blueRunDetailPage.clickTab('changes');
        blueRunDetailPage.validateEmpty();
    }
};

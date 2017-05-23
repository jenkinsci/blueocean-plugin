const JOB = 'noStagesFail';
/** @module failing
 * @memberof testcases
 * @description TEST: basic tests around the failing pipeline. Test whether the result is not collapsed.
 */
module.exports = {
    /**
     * Create Pipeline Job "noStagesFail", create a simple pipeline, that will produce a failure
     * @param browser
     */
    'Step 01': function (browser) {
        browser.login();
        const pipelinesCreate = browser.page.pipelineCreate().navigate();
        // we have used the noStages script as basis
        pipelinesCreate.createPipeline(JOB, 'noStagesFailing.groovy');
    },

    /**
     * Build Pipeline Job
     * @param browser
     */
    'Step 02': function (browser) {
        const pipelinePage = browser.page.jobUtils().forJob('noStagesFail');
        // start to build the pipeline
        pipelinePage.buildStarted(function() {
            // Reload the job page and check that there is a build started.
            pipelinePage
                .forRun(1)
                .waitForElementVisible('@executer');
        });
    },
    /**
     * Check whether the resultItemas are collapsing as expected.
     * @param browser
     */
    // now testing JENKINS-37666
    'Step 03': function (browser) {
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(JOB, 'jenkins', 1);
        // we want to analyse the result after the job has finished
        browser.waitForJobRunEnded(JOB, function() {
            // the failure should collapse
            blueRunDetailPage.clickFirstResultItemFailure(false);
            // test whether the expand works
            blueRunDetailPage.clickFirstResultItem();
            // now click again so the result collapse again
            blueRunDetailPage.clickFirstResultItem(false);
            // now click the node again and see whether only one code is visible
            blueRunDetailPage.clickFirstResultItem();
            // we now need to get all visible code blocks, but there should be no more then one
            browser.elements('css selector', 'pre', function (codeCollection) {
                this.assert.equal(typeof codeCollection, "object");
                this.assert.equal(codeCollection.status, 0);
                // JENKINS-36700 in fail all code should be closed besides one
                // however if the browser is too quick there can still be two open
                this.assert.equal(codeCollection.value.length <= 2, true);
            });

            // as it has failed, should get a replay button
            blueRunDetailPage.waitForElementVisible('.replay-button');



        });

    },

    /**
     * Check that the failed item shows up and has a replay icon
     */
    'Step 04' : function(browser) {
        var blueActivityPage = browser.page.bluePipelineActivity().forJob(JOB, 'jenkins');
        blueActivityPage.waitForRunFailureVisible(JOB, '1');
        blueActivityPage.waitForElementVisible('.replay-button');
    },

    /**
     * As it has failed, we can rerun the job, check that it runs, and then result is still failure.
     */
    'Step 05' : function(browser) {
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(JOB, 'jenkins', 1);

        //click the re run button
        blueRunDetailPage.clickReRunButton();

        //Ccheck that it runs and we could stop if if we want to
        blueRunDetailPage.waitForElementVisible('.progress-spinner');
        blueRunDetailPage.waitForElementPresent('.stop-button');

        // this will show up when it has finished replaying
        blueRunDetailPage.waitForElementVisible('.replay-button');

    }
};

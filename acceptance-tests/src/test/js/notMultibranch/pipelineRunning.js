const jobName = 'pipeRun';
/** @module pipelineRunning
 * @memberof notMultibranch
 * @description Check can run non multibranch pipelines from activity
 */
module.exports = {
    /** Create pipeline Job */
    'Step 01': function (browser) {
        const pipelinesCreate = browser.page.pipelineCreate().navigate();
        // we have used the noStages script as basis
        pipelinesCreate.createPipeline(jobName, 'noStagesSmallWait.groovy');
    },

    
    /** Build pipeline Job*/
    'Step 02': function (browser) {
        var blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        blueActivityPage.waitForElementVisible('.run-button');
        
        // run the job
        blueActivityPage.click('.run-button');
        blueActivityPage.waitForElementVisible('@toastOpenButton')
        
        //check it spins and then is done
        const rowSelector = `[data-pipeline='${jobName}'][data-runid='1']`;
        blueActivityPage.waitForElementVisible(rowSelector);
        blueActivityPage.waitForElementVisible('.progress-spinner');
        blueActivityPage.waitForElementVisible('.success');         
        blueActivityPage.waitForElementNotPresent('.progress-spinner');       

        browser.elements('css selector', rowSelector, function(res) {
            browser.assert.equal(1, res.value.length, 'Correct number of runs started');
        })
    },
    
    
    /** Build pipeline Job again */
    'Step 03': function (browser) {
        var blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        blueActivityPage.waitForElementVisible('.run-button');
        
        // run the job
        blueActivityPage.click('.run-button');
        blueActivityPage.waitForElementVisible('@toastOpenButton')
        
        //check it spins and then is done
        const rowSelector = `[data-pipeline='${jobName}'][data-runid='2']`;
        blueActivityPage.waitForElementVisible(rowSelector);
    },

};

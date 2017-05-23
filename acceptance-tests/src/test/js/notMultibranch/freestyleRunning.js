const jobName = 'freeRun';
/** @module freestyleRunning
 * @memberof notMultibranch
 * @description Freestyle running from activity screen
 */
module.exports = {
    /** Create freestyle Job */
    'Step 01': function (browser) {
        const freestyleCreate = browser.page.freestyleCreate().navigate();
        freestyleCreate.createFreestyle(jobName, 'freestyle.sh');
    },

    /** Build freestyle Job*/
    'Step 02': function (browser) {
        var blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
        blueActivityPage.waitForElementVisible('.run-button');

        // run the job
        blueActivityPage.click('.run-button');
        blueActivityPage.waitForElementVisible('@toastOpenButton');

        //check it spins and then is done  
        blueActivityPage.waitForElementVisible('.run-button.btn-secondary');
        const rowSelector = `[data-pipeline='${jobName}'][data-runid='1']`;
        blueActivityPage.waitForElementVisible(rowSelector);
        blueActivityPage.waitForElementVisible('.progress-spinner');
        blueActivityPage.waitForElementVisible('.success');
        blueActivityPage.waitForElementNotPresent('.progress-spinner');

        browser.elements('css selector', rowSelector, function (res) {
            browser.assert.equal(1, res.value.length, 'Correct number of runs started');
        })
    },

};

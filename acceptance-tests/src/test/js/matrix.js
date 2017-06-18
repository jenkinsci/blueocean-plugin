const jobName = 'matrixJob';
/** @module matrix
 * @memberof matrix
 * @description Tests that matrix jobs link to classic jenkins.
 */
module.exports = {
    /** Create matrix Job */
    'Create job': (browser) => {
        const matrixCreate = browser.page.matrixCreate().navigate();
        matrixCreate.createMatrix(jobName);
    },

    'Check matrix link': (browser) => {
        const pipelinePage = browser.page.bluePipelines().navigate();

        pipelinePage.waitForElementVisible('.pipelineRedirectLink');

        browser.useXpath().waitForElementVisible(`//*/a[contains(@class, "pipelineRedirectLink") and contains(@href, "/job/${jobName}/")]`)
    }
}

/** @module freestylePing
 * @memberof karaoke
 * @description REGRESSION TEST: logs not tailing with freestyle
 *
 * We will use a simple script which pings 10 times a certain url.
 * To provoke that the browser has to scroll we will resize the browser and then test the scrollHeight of the
 * parent container. If that is bigger then 0, that means that we have scrolled and the karaoke works as expected.
 */
// The name of the job
const jobName = 'pingTest';
module.exports = {
/** Create freestyle Job "ping"*/
    'Step 01': function (browser) {
        // simple script to ping 10 times a certain url
        const freestyleCreate = browser.page.freestyleCreate().navigate();
        freestyleCreate.createFreestyle(jobName, 'freestylePing.sh');
    },
/** Build freestyle Job*/
    'Step 02': function (browser) {
        // build the job
        const freestylePage = browser.page.jobUtils().forJob(jobName);
        freestylePage.buildStarted(function () {
            // Reload the job page and check that there was a build done.
            freestylePage
                .forRun(1)
                .waitForElementVisible('@executer');
        })
    },
/** Check Job Blue Ocean run detail page - karaoke*/
    'Step 03': function (browser) {
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(jobName, 'jenkins', 1);

        // first resize the browser so we get quickly a level where we will have to scroll
        browser.resizeWindow(600, 600);
        // wait until the job has finished
        blueRunDetailPage.waitForJobRunEnded(jobName)
            .waitForElementVisible('pre')
            .fullLogButtonNotPresent()
            .expect.element('pre').text.to.contain('Finished: SUCCESS');
        // make sure we have scrolled
        blueRunDetailPage.validateScrollToBottom();
        // make the browser big again
        browser.resizeWindow(1680, 1050);

    },


};

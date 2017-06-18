const jobName = 'pipeRunArtifacts';
/** 
 * @module artifacts
 * @memberof artifacts
 * @description Makes sure artifacts show correctly.
 */
module.exports = {
    /** Create pipeline Job */
    'Step 01': function (browser) {
        const pipelinesCreate = browser.page.pipelineCreate().navigate();
    
        pipelinesCreate.createPipeline(jobName, 'lots-of-artifacts.groovy');
    },

     /** Build Pipeline Job*/
    'Step 02': function (browser) {
      var blueActivityPage = browser.page.bluePipelineActivity().forJob(jobName, 'jenkins');
      blueActivityPage.waitForElementVisible('@runButton');
      
      // run the job
      blueActivityPage.click('@runButton');
      blueActivityPage.waitForElementVisible('@toastOpenButton');
      blueActivityPage.waitForRunSuccessVisible(jobName, '1');

    },

    'Step 03': function (browser) {
        const blueRunDetailPage = browser.page.bluePipelineRunDetail().forRun(jobName, 'jenkins', 1);
        
        blueRunDetailPage.waitForElementVisible('section.BasicHeader--success');
        
        blueRunDetailPage.clickTab('artifacts');
        blueRunDetailPage.waitForElementVisible('.artifacts-info');
    }

};

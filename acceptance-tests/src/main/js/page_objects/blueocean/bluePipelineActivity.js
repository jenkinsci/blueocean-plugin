/** @module bluePipelineActivity
 * @memberof page_objects
 * @description Represents the listing of the activities in blueocean
 *
 * @example
 *   const blueActivityPage = browser.page.bluePipelineActivity().forJob('my-pipeline', 'jenkins');
 * */
const url = require('../../util/url');
const pageHelper = require('../../util/pageHelper');
//oh man, I miss es6 import :(
const sanityCheck = pageHelper.sanityCheck;

function activityRowSelector(pipeline, runId) {
    // Fixme: Can we parameterise the "elements" below?
    return `.activity-table .JTable-row[data-pipeline='${pipeline}'][data-runid='${runId}']`;
}

const activityTableEntries = '.activity-table .JTable-row[data-pipeline]';

module.exports = {
    elements: {
        pipelinesNav: '.Header-topNav nav a[href="/blue/pipelines"]',
        emptyStateShoes: '.PlaceholderContent.NoRuns',
        activityTable: '.activity-table',
        activityTableEntries: activityTableEntries,
        runButton: 'a.run-button',
        toastOpenButton: {
            selector: '//div[@class="toast default"]/a[@class="action" and text()="Open"]',
            locateStrategy: 'xpath',
        },
    }
};
module.exports.commands = [{
    /**
    * Returns the config page of a certain job
    * @param jobName {String} name of the job to configure
    * @param branchName {String} name of the branch (may be the same as jobname, in case of freestyle
    * @returns {Object} self - nightwatch page object
    */
    forJob: function(jobName, orgName) {
        if (!orgName) {
            orgName = 'jenkins';
        }
        const pageUrl = this.api.launchUrl + url.viewPipelineActivity(orgName, jobName);
        this.jobName = jobName;
        this.orgName = orgName;
        return this.navigate(pageUrl);
    },
    /**
     * Different test on general elements that should be visible on the page
     * @returns {Object} self - nightwatch page object
     */
    assertBasicLayoutOkay: function(jobName) {
        const baseHref = url.viewPipeline('jenkins', (jobName?jobName:this.jobName));
        this.waitForElementVisible('@pipelinesNav');
        this.waitForElementVisible('.Header-topNav a');
        this.waitForElementVisible('.Site-footer');
        // Test the end of the active url and make sure it's on the
        // expected activity page.
        if (jobName) {
            this.assert.urlEndsWith(jobName + '/activity');
        } else {
            this.assert.urlEndsWith('/activity');
        }
    },
    /**
     * Different test on general elements that should be visible on an empty activity page
     * @returns {Object} self - nightwatch page object
     */
    assertEmptyLayoutOkay: function(jobName) {
        this.assertBasicLayoutOkay(jobName);
        this.waitForElementVisible('@emptyStateShoes');
    },
    /**
     * Wait for a specific run to appear in the activity table
     * @param pipeline name of the pipeline
     * @param runId id of the run
     */
    waitForRunVisible: function(pipeline, runId) {
        this.waitForElementVisible(activityRowSelector(pipeline, runId));
    },
    /**
     * Wait for a specific run to appear in the activity table as a success
     * @param pipeline name of the pipeline
     * @param runId id of the run
     */
    waitForRunSuccessVisible: function(pipeline, runId) {
        this.waitForRunVisible(pipeline, runId);
        const resultRowSelector = activityRowSelector(pipeline, runId);
        this.waitForElementVisible(`${resultRowSelector} .success`);
    },
    /**
     * Wait for a specific run to appear in the activity table as a failure
     * @param runName name of the job
     */
    waitForRunFailureVisible: function(pipeline, runId) {
        this.waitForRunVisible(pipeline, runId);
        const resultRowSelector = activityRowSelector(pipeline, runId);
        this.waitForElementVisible(`${resultRowSelector} .failure`);
    },    

    /**
     * Wait for a specific run to appear in the activity table as unstable
     * @param runName name of the job
     */
    waitForRunUnstableVisible: function(pipeline, runId) {
        this.waitForRunVisible(pipeline, runId);
        const resultRowSelector = activityRowSelector(pipeline, runId);
        this.waitForElementVisible(`${resultRowSelector} .unstable`);
    },
    /**
     * Wait for a specific run to appear in the activity table as running
     * @param runName name of the job
     * @param [callback] {Function} - callback to be invoke when finished, will pass the sse event to the callback
     */
    waitForRunRunningVisible: function(pipeline, runId) {
        this.waitForRunVisible(pipeline, runId);
        const resultRowSelector = activityRowSelector(pipeline, runId);
        this.waitForElementVisible(`${resultRowSelector} .running`);
    },
    /**
     * Wait for a specific run to appear in the activity table as paused
     * @param runName name of the job
     * @param [callback] {Function} - callback to be invoke when finished, will pass the sse event to the callback
     */
    waitForRunPausedVisible: function(pipeline, runId) {
        this.waitForRunVisible(pipeline, runId);
        const resultRowSelector = activityRowSelector(pipeline, runId);
        this.waitForElementVisible(`${resultRowSelector} .paused`);
    },

    /**
     * Inspect that result screen runs, shows a stage graph, and completes.
     */
    assertStageGraphShows: function() {
      //check results look kosher:
      this.waitForElementVisible('.progress-spinner.running');                           
      this.waitForElementVisible('.BasicHeader--running');
      
      this.waitForElementVisible('.pipeline-node-selected');                  
      this.waitForElementVisible('.download-log-button');                  
      this.waitForElementVisible('.pipeline-selection-highlight');                    
      this.waitForElementVisible('.pipeline-connector');     
      this.waitForElementVisible('.pipeline-node-hittarget');     
      this.waitForElementVisible('.BasicHeader--success');  

    },
    
    /**
    * Click css selector of a specific tab
    * @param tab {string} the tab we want to select
    * @returns {Object} self - nightwatch page object
    */
    clickTab: function (tab) {
        var self = this;
        return url.clickTab(self, tab);
    },
    /**
     * Count the activities in the table and compare them with the expected value.
     * @param expected {Number}
     */
    assertActivitiesToBeEqual: function (expected) {
        var self = this;
        const browser = this.api;
        self.waitForElementVisible('@activityTableEntries');
        browser.elements('css selector', activityTableEntries, function (codeCollection) {
            this.assert.equal(codeCollection.value.length, expected);
        });
    },
    /**
     * On Activity Page click the run button, then click the open in toast
     * and then validate that we are on the detail page
     * @returns {Object} self - nightwatch page object
     */
    clickRunButtonAndOpenDetail: function () {
        var self = this;
        const browser = this.api;
        // first press the runButton
        self.waitForElementVisible('@runButton');
        self.click('@runButton');
        // now press the toast "open" link
        self.waitForElementVisible('@toastOpenButton');
        self.click('@toastOpenButton', function (response) {
            sanityCheck(self, response);
        });
        return self;
    }
}];

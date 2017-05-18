/**
 * @module classicRun
 * @memberof page_objects
 * @description Represents a Job Run page on classic Jenkins
 */

exports.elements = {
    consoleOutput: {
        selector: '//a[text()="Console Output"]',
        locateStrategy: 'xpath',
    }
};
exports.commands = [
    {
        /**
         * Navigate to a Job run page.
         * @param jobName {String} name of the job.
         * @param runId {String} The run Id of the run on the job.
         * @returns {Object} self - nightwatch page object
         */
        navigateToRun: function(jobName, runId) {
            const runUrl = this.api.launchUrl + '/job/' + jobName + '/' + (runId?runId:'1');
            this.navigate(runUrl);
            this.waitForElementVisible('@consoleOutput');
            return this;
        }
    }
];
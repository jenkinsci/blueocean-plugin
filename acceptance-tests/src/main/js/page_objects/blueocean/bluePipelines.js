/** @module bluePipelines
 * @memberof page_objects
 * @description Represents the listing of all pipelines in blueocean
 *
 * @example
 *   var bluePipelinesPage = browser.page.bluePipelines().navigate();
 * */
const url = require('../../util/url');
const pipelinesUrl = url.viewAllPipelines();

function pipelineRowSelector(pipeline) {
    return `.pipelines-table .JTable-row[data-pipeline='${pipeline}']`;
}

module.exports = {

    url: function () {
        return this.api.launchUrl + pipelinesUrl;
    },
    elements: {
        pipelinesNav: '.Header-topNav nav a[href="'+pipelinesUrl+'"]',
        newPipelineButton: '.btn-new-pipeline',
        pipelinesTable: '.pipelines-table',
    }
};

// Nightwatch commands.
// http://nightwatchjs.org/guide#writing-commands
// Seems like we could use a Nightwatch assertions for some of these, but then that would be global.
// Prefer it this way, local to the page.
module.exports.commands = [{
    /**
     * Different test on general elements that should be visible on the page
     * @returns {Object} self - nightwatch page object
     */
    assertBasicLayoutOkay: function() {
        this.waitForElementVisible('@pipelinesNav');
        this.waitForElementVisible('@pipelinesTable');
        this.waitForElementVisible('.Site-footer');
    },
    assertJob: function(jobName) {
        this.waitForElementVisible(pipelineRowSelector(jobName));
    },
    countJobToBeEqual: function(browser, jobName, count) {
        browser.elements('css selector', pipelineRowSelector(jobName), function (codeCollection) {
            this.assert.equal(codeCollection.value.length, count);
        });
    },
    navigateLanguage: function(language) {
        return this.navigate(this.api.launchUrl + pipelinesUrl + '?language=' + language);
    }
}];

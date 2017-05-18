/**
 * @module blueMultiBranchPipeline
 * @memberof page_objects
 * @description Represents the detail page of a pipeline/job in blueocean
 *
 * @example
 *   const bluePipeline = browser.page.bluePipeline().forPipeline('pipelineA');
 */

const url = require('../../util/url');
const pageHelper = require('../../util/pageHelper');

module.exports = {
    // selectors
    elements: {
        exitToClassicWidget: 'a.main_exit_to_app',
    }
};

module.exports.commands = [{
    /**
     * Navigate to a pipeline page.
     *
     * @example
     *   const blueRunDetailPage = browser.page.bluePipelineRunDetail()
     .forRun(jobNameFreestyle, 'jenkins', 1);
     *
     * @param {String} jobName
     * @param {String|Number} branchName - either the branchName o the buildNumber
     * @returns {Object} self - nightwatch page object
     */
    forPipeline: function (jobName) {
        this.jobName = encodeURIComponent(jobName);
        this.orgName = 'jenkins';
        return this.navigate(this.pageUrl());
    },

    /**
     * Will return either a relative or an absolute URL
     * @param {Boolean} relative
     * @returns {String} url
     */
    pageUrl: function(relative) {
        var pageUrl = url.makeRelative(url.viewPipelineActivity(this.orgName, this.jobName));
        return !relative ? this.api.launchUrl + pageUrl : pageUrl;
    },

    /**
     * Different test on general elements that should be visible on the page
     * @returns {Object} self - nightwatch page object
     */
    assertBasicLayoutOkay: function() {
        this.waitForElementVisible(url.tabSelector('activity'));
        this.waitForElementVisible(url.tabSelector('branches'));
        this.waitForElementVisible(url.tabSelector('pr'));
        this.waitForElementVisible('@exitToClassicWidget');
        return this;
    }
}];

const url = require('../../../../main/js/util/url');
/** @module jobUtils
 * @memberof page_objects
 * @description Represents the "new Item" page of classic jenkins.
 * @example const freestylePage = browser.page.jobUtils().forJob('hijo');
 * */
exports.elements = {
    newItem: {
        selector: '//a[text()="New Item"]',
        locateStrategy: 'xpath',
    },
    build: {
        selector: '//a[text()="Build Now"]',
        locateStrategy: 'xpath',
    },
    queued: 'div[title="Expected build number"]',
    indexing: {
        selector: '//a[text()="Run Now"]',
        locateStrategy: 'xpath',
    },
    executer: {
        selector: '//span[contains(text(),"Started by")]',
        locateStrategy: 'xpath',
    },
    builds: '#buildHistory .build-row-cell .icon-blue'
};
exports.commands = [{
    /**
     * Returns the config page of a certain job
     * @example  const freestylePage = browser.page.jobUtils().forJob('hijo');
     * @param jobName {String} name of the job to configure
     * @param [suffix] {String} we may need to add a suffix to the final url
     * @returns {Object} self - nightwatch page object
     */
    forJob: function(jobName, suffix) {
        var jobUrl = url.getJobUrl(this.api.launchUrl, jobName);
        this.jobName = jobName;
        return this.navigate(suffix ? jobUrl + suffix : jobUrl);
    },
    /**
     * Returns the config page of a certain run
     * @example  freestylePage.forRun('1');
     * @param runId the id of a specific run
     * @returns {Object} self - nightwatch page object
     */
    forRun: function(runId) {
        var runUrl = url.getJobUrl(this.api.launchUrl, this.jobName) + runId;
        return this.navigate(runUrl);
    },
    /**
     * Navigate to a certain url
     * @example  freestylePage.forUrl(someUrl);
     * @param url {String} the url to navigate to
     * @param jobName {String} name of the job to configure
     * @returns {Object} self - nightwatch page object
     */
    forUrl: function (url, jobName) {
        this.jobName = jobName;
        return this.navigate(url);
    },
    newItem: function() {
        this.waitForElementVisible('@newItem');
        this.click('@newItem');
        this.waitForElementVisible('#add-item-panel', function() {
            this.moveClassicBottomStickyButtons();
        });
    },
    /**
     * Try to build the job, invoke callback when build is complete
     * @example
freestylePage.build(function() {
    // Reload the job page and check that there was a build done.
    freestylePage = browser.page.jobUtils().forJob('my-pipeline');
    freestylePage.waitForElementVisible('@builds');
});
     * @param [onBuildComplete] {Function} - callback to be invoke when finished
     * @see {@link module:custom_commands.waitForJobRunEnded}
     * @returns {Object} self - nightwatch page object
     */
    build: function(onBuildComplete) {
        this.waitForElementVisible('@build');
        this.click('@build');
        if (onBuildComplete) {
            this.api.waitForJobRunEnded(this.jobName, onBuildComplete);
        }
        return this;
    },
    /**
     * Try to build the job, invoke callback when build is started
     * @example
freestylePage.buildStarted(function () {
    // Reload the job page and check that there was a build done.
    freestylePage
        .forRun(1)
        .waitForElementVisible('@executer');
})
     * @param [onBuildStarted] {Function} - callback to be invoke when finished
     * @see {@link module:custom_commands.waitForJobRunStarted}
     * @returns {Object} self - nightwatch page object
     */
    buildStarted: function(onBuildStarted) {
        this.waitForElementVisible('@build');
        this.click('@build');
        if (onBuildStarted) {
            this.api.waitForJobRunStarted(this.jobName, onBuildStarted);
        }
        return this;
    },
    /**
     * Build and wait until job is in the queue.
     * @example freestylePage.buildQueued();
     * @returns {Object} self - nightwatch page object
     */
    buildQueued: function() {
        this.waitForElementVisible('@build');
        this.click('@build');
        this.waitForElementVisible('@queued');
        return this;
    },
    /**
     * Multibranch projects are needed to be indexed.
     * This helper will click on the indexing selector and call the callback when the run has started
     * @example masterJob.indexingStarted(); // start a new build by starting indexing
     * @param [onIndexingStarted] {Function} - callback to be invoke when finished
     * @see {@link module:custom_commands.onIndexingStarted}
     * @returns {Object} self - nightwatch page object
     */
    indexingStarted: function(onIndexingStarted) {
        this.waitForElementVisible('@indexing');
        this.click('@indexing');
        if (onIndexingStarted) {
            this.api.waitForJobRunStarted(this.jobName, onIndexingStarted);
        }
        return this;
    }
}];

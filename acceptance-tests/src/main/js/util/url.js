/** @module util/url */
module.exports = {
    /**
     * In case we have multiBranch projects or we are using folder,
     * we need to calculate the path, prefix every folder with ```/job/```
     * @param launchUrl - this is normally equal to this.api.launchUrl
     * @param jobName {String} name of the job to configure
     * @returns url {String} calculate the url of a mutlibranch
     */
    getJobUrl: function (launchUrl, jobName) {
        const nameArray = jobName.split('/');
        var jobUrl = launchUrl;
        jobUrl = jobUrl.replace(/\/$/, ""); // trim trailing slash if there is one
        nameArray.map(function (item) {
            jobUrl += '/job/' + item + '/';
        });
        return jobUrl;
    },
    /**
     * make it relative
     * @param url {String}
     * @returns url {String}
     */
    makeRelative: function(url) {
        return url.indexOf("/") ?
            url.substr(1) :
            url;
    },
    /**
     * get the url to set the Build Executor
     * @returns {String}
     */
    configureBuildExecutor: function () {
        return '/computer/(master)/configure';
    },
    /**
     * get the url to list all pipelines
     * @returns {string}
     */
    viewAllPipelines: function() {
        return '/blue/pipelines';
    },
    /**
     * get the url to list all pipelines
     * @returns {string}
     */
    viewPipeline: function(orgName, jobName) {
        return '/blue/organizations/' + orgName + '/' + jobName;
    },
    /**
     * Return the path to the activity defined by the orgName and jobName
     * @param orgName {String} name of the organisation
     * @param jobName {String} name of the job
     * @returns {string}
     */
    viewPipelineActivity: function(orgName, jobName) {
        return this.viewPipeline(orgName, jobName) + '/activity';
    },
    /**
     * returns the url for a job detail in blueocean
     * @param orgName name of the organisation
     * @param jobName {String} name of the job
     * @param branchName {String} name of the branch (may be the same as jobname, in case of freestyle
     * @param buildNumber {String} number the build
     * @returns {string}
     */
    viewRunPipeline: function(orgName, jobName, branchName, buildNumber) {
        return '/blue/organizations/' + orgName + '/' + jobName + '/detail/' + branchName + '/' + buildNumber;
    },
    /**
     * Return css selector of a specific tab
     * @param tabName {string} the tab we want to select
     * @returns {string}
     */
    tabSelector: function(tabName) {
        return 'nav a.' + tabName;
    },
    /**
     * click a certain tab and validate that url has changed
     * @param self {Object} self - nightwatch page object
     * @param tab name of the tab you want to click
     */
    clickTab: function(self, tab) {
        const tabSelector = this.tabSelector(tab);
        const browser = self.api;
        self.waitForElementVisible(tabSelector);
        self.click(tabSelector);
        browser.url(function (response) {
                self.assert.equal(typeof response, "object");
                self.assert.equal(response.status, 0);
                // did we changed the url on  change?
                self.assert.equal(response.value.includes(tab), true);
            });
        return self;
    },
    /**
     * get the url to create a pipeline
     * @returns {string}
     */
    createPipeline: function() {
        return '/blue/create-pipeline/';
    },
};

const url = require('../../util/url');
/** @module multibranchCreate
 * @memberof page_objects
 * @description Represents the "new Item" page of classic jenkins but for multiBranch
 * @example const branchCreate = browser.page.multibranchCreate().forJob(anotherFolders.join('/'));
 * */
const suffix = 'newJob';
module.exports = {
    url: function () {
        return this.api.launchUrl + '/view/All/' + suffix;
    },
    elements: {
        nameInput: '#name',
        multibranchType: 'li.org_jenkinsci_plugins_workflow_multibranch_WorkflowMultiBranchProject',
        submit: '#newFormSubmitButtonForATH',
        configForm: 'form[name="config"]',
        configSave: '#newFormSubmitButtonForATH',
        button: {
            selector: '//button[@path="/hetero-list-add[sources]"]',
            locateStrategy: 'xpath',
        },
        scriptHook: {
            selector: '//input[@path="/sources/source/remote"]',
            locateStrategy: 'xpath',
        },
        gitA: {
            selector: '//a[text()="Git"]',
            locateStrategy: 'xpath',
        }
    }
};
module.exports.commands = [{
        /**
         * Returns the config page of a certain job
         * @example const branchCreate = browser.page.multibranchCreate().forJob(anotherFolders.join('/'));
         * @param jobName {String} name of the job to configure
         * @returns {Object} self - nightwatch page object
         */
    forJob: function(jobName) {
        var jobUrl = url.getJobUrl(this.api.launchUrl, jobName);
        this.jobName = jobName;
        return this.navigate(jobUrl + '/' + suffix);
    },
    newItem: function(jobName) {
        var jobUrl = url.getJobUrl(this.api.launchUrl, jobName);
        return this.navigate(jobUrl + '/newJob');
    },

    /**
     * @example // Let us create a multibranch object in the nested folders
branchCreate.createBranch(multiBranchJob, pathToRepo);
     * @param folderName {String} where should be we create the multiBranch project in
     * @param path {String} where does the git repo exist? Will be injected in the input field
     */
    createBranch: function (folderName, path) {
        var self = this;

        self.waitForJobDeleted(folderName);

        self.setValue('@nameInput', folderName);

        self.waitForElementPresent('@multibranchType');
        self.click('@multibranchType');
        self.waitForElementPresent('@submit');
        self.click('@submit');

        // We should now be on the configuration page for
        // the multibranch job.
        self.waitForElementPresent('@configForm');
        self.moveClassicBottomStickyButtons();
        self.waitForElementPresent('@button');
        self.click('@button');

        // Create a job with a git repo. Otherwise don't.'
        if (path) {
            self.waitForElementPresent('@gitA');
            self.click('@gitA');
            self.waitForElementPresent('@scriptHook');
            self.setValue('@scriptHook', path);
        }

        self.waitForElementPresent('@configSave')
            .click('@configSave');

    },
}];
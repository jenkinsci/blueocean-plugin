/**
 * @module blueCreate
 * @memberof page_objects
 * @description Represents the "create pipeline" page
 *
 * @example
 *   var blueCreatePage = browser.page.bluePipelineCreate().navigate();
 * */
const url = require('../../util/url');

module.exports = {
    url: function() {
        return this.api.launchUrl + url.createPipeline();
    },
    elements: {
        gitCreationButton: '.scm-provider-list .git-creation',
        repositoryUrlText: '.text-repository-url input',
        newCredentialTypeSystemSSh: '.credentials-type-picker .RadioButtonGroup-item:nth-child(3)',
        createButton: '.git-step-connect .button-create-pipeline',
        openPipelineButton: '.git-step-completed .button-open-pipeline',
    }
};

module.exports.commands = [{
    assertCompleted: function() {
        this.waitForElementVisible('.git-step-completed');
        this.waitForElementVisible('@openPipelineButton');
    }
}];

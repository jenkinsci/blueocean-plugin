/** @module freestyleCreate
 * @memberof page_objects
 * @description Represents the freestyle creation page of classic jenkins.
 *
 * */

module.exports = {
    url: function () {
        return this.api.launchUrl + '/view/All/newJob';
    },
    elements: {
        nameInput: '#name',
        freestyleType: 'li.hudson_model_FreeStyleProject',
        submit: '#newFormSubmitButtonForATH',
        jobIndexPageHeader: 'h1.job-index-headline'
    }
};

module.exports.commands = [{
    /**
     * Returns the create freestyle page for a certain job and creates the freestyle job
     * @param jobName {String} name of the job to configure
     * @param script{String} the name of the script that shoould be used to be injected. Has to
     * be present in ROOT/src/test/resources/test_scripts
     * @param {Function} [oncreated] - callback to be invoke when finished, will expect a traditional node callback function
     */
    createFreestyle: function(jobName, script, oncreated) {
        var self = this;
        
        self.waitForJobDeleted(jobName);

        self.setValue('@nameInput', jobName);
        self.waitForElementPresent('@submit');
        self.click('@freestyleType');
        self.click('@submit');

        if (!oncreated) {
            // If no oncreated function was supplied then we manufacture
            // a dummy. This ensures that this function does not return
            // immediately.
            oncreated = function() {};
        }

        // Clicking submit above should result in us ending up on the
        // job config page. Set the freestyle script.
        self.api.page.freestyleConfig()
            .moveClassicBottomStickyButtons()
            .setFreestyleScript(script)
            .click('@save');

        // Wait for the signal that the config page has saved
        // and we're back on the job index page.
        self.waitForElementPresent('@jobIndexPageHeader');
    },
}];
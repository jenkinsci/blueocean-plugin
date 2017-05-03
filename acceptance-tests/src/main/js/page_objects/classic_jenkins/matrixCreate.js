/** @module matrixCreate
 * @memberof page_objects
 * @description Represents the matrix creation page of classic jenkins.
 *
 * */

module.exports = {
    url: function () {
        return this.api.launchUrl + '/view/All/newJob';
    },
    elements: {
        nameInput: '#name',
        matrixType: 'li.hudson_matrix_MatrixProject',
        submit: '#newFormSubmitButtonForATH',
        jobIndexPageHeader: 'h1.matrix-project-headline'
    }
};

module.exports.commands = [{
    /**
     * Returns the create matrix page for a certain job and creates the matrix job
     * @param jobName {String} name of the job to configure
     * @param {Function} [oncreated] - callback to be invoke when finished, will expect a traditional node callback function
     */
    createMatrix: function(jobName, oncreated) {
        var self = this;
        
        self.waitForJobDeleted(jobName);

        self.setValue('@nameInput', jobName);
        self.waitForElementPresent('@submit');
        self.click('@matrixType');
        self.click('@submit');

        if (!oncreated) {
            // If no oncreated function was supplied then we manufacture
            // a dummy. This ensures that this function does not return
            // immediately.
            oncreated = function() {};
        }

        // Reusing the freeStyle config code atm since we arent actaully doing anything specific.
        self.api.page.freestyleConfig()
            .moveClassicBottomStickyButtons()
         //   .setFreestyleScript(script)
            .click('@save');

        // Wait for the signal that the config page has saved
        // and we're back on the job index page.
        self.waitForElementPresent('@jobIndexPageHeader');
    },
}];
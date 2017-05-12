/** @module pipelineConfig
 * @memberof page_objects
 * @description Represents the pipeline config page of classic jenkins. Used from within @see {@link module:pipelineCreate}
 * @example
 self.api.page.pipelineConfig().forJob(jobName)
 .setPipelineScript(script)
 .click('@save', oncreated);
 * */
var fs = require('fs');

exports.elements = {
    scriptInput: '#workflow-editor-1 .ace_text-input',
    configForm: 'form[name="config"]',
    save: '#newFormSubmitButtonForATH'
};

exports.commands = [
    {
        /**
         * Returns the config page of a certain job
         * @param jobName {String} name of the job to configure
         * @returns {Object} self - nightwatch page object
         */
        forJob: function(jobName) {
            var jobUrl = this.api.launchUrl + 'job/' + jobName + '/configure';
            return this.navigate(jobUrl);
        },
        /**
         * Set the pipeline script to the correct input field and then saves the form
         * @param script {String} the name of the script that shoould be used to be injected. Has to
         * be present in ROOT/src/test/resources/test_scripts
         * @returns {Object} self - nightwatch page object
         */
        setPipelineScript: function (script) {
            var scriptText = readTestScript(script);
    
            // Need to wait for the ACE Editor to fully render on the page
            this.waitForElementPresent('@scriptInput');
            this.api.execute(function (selector, scriptText) {
                var targets = document.getElementsBySelector(selector);
                targets[0].aceEditor.setValue(scriptText);
                return true;
            }, ['#workflow-editor-1', scriptText]);
            
            return this;
        }
    }
];
/**
 * Synchrony read the script file if exists
 * @param script {String} - file name
 * @returns {*}
 */
function readTestScript(script) {
    var fileName = 'src/test/resources/test_scripts/' + script;
    
    if (!fs.existsSync(fileName)) {
        // It's not a script file.
        // Must be a raw script text.
        return script;
    }
    
    return fs.readFileSync(fileName, 'utf8');
}
/** @module freestyleConfig
 * @memberof page_objects
 * @description Represents the freestyle config page of classic jenkins. Used from within @see {@link module:freestyleCreate}
 * @example
 self.api.page.freestyleConfig().forJob(jobName)
 .setFreestyleScript(script)
 .click('@save', oncreated);
 * */
var fs = require('fs');

exports.elements = {
    button: {
        selector: '//button[@path="/hetero-list-add[builder]"]/parent::span/parent::span',
        locateStrategy: 'xpath',
    },
    shell: {
        selector: '//a[text()="Execute shell"]',
        locateStrategy: 'xpath',
    },
    scriptHook: {
        selector: '//textarea[@name="command"]',
        locateStrategy: 'xpath',
    },
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
            const jobUrl = this.api.launchUrl + 'job/' + jobName + '/configure';
            return this.navigate(jobUrl);
        },
        /**
         * Set the freestyle script to the correct input field and then saves the form
         * @param script {String} the name of the script that shoould be used to be injected. Has to
         * be present in ROOT/src/test/resources/test_scripts
         * @returns {Object} self - nightwatch page object
         */
        setFreestyleScript: function (script) {
            const scriptText = readTestScript(script);
            this.waitForElementPresent('@configForm');
            this.waitForElementPresent('@save');
            this.waitForElementVisible('@button')
                .click('@button')
                .waitForElementVisible('@shell')
                .click('@shell')
                .waitForElementPresent('@scriptHook');
            // we need to do the following to inject the script based on
            // https://github.com/jenkinsci/acceptance-test-harness/blob/master/src/main/java/org/jenkinsci/test/acceptance/po/CodeMirror.java
            this.api.execute(function (selector, scriptText) {
                const cmElem = document.evaluate(
                    selector, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null
                ).singleNodeValue;
                const codemirror = cmElem.CodeMirror;
                if (codemirror == null) {
                    console.error('CodeMirror object not found!');
                }
                codemirror.setValue(scriptText);
                codemirror.save();
                return true;
            }, ['//*[@name="command"]/following-sibling::div', scriptText]);
            this.waitForElementPresent('@save');

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
    const fileName = 'src/test/resources/test_scripts/' + script;

    if (!fs.existsSync(fileName)) {
        // It's not a script file.
        // Must be a raw script text.
        return script;
    }
    
    return fs.readFileSync(fileName, 'utf8');
}
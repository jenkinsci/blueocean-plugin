const url = require('../../util/url');
const configureBuildExecutor = url.configureBuildExecutor();
/** @module computer
 * @memberof page_objects
 * @description Represents the configuration page of executors in classic jenkins
 *
 * @example
 *   const configure = browser.page.computer().navigate();
 * */
module.exports = {

    url: function () {
        return this.api.launchUrl + configureBuildExecutor;
    },
    elements: {
        computer: 'img.icon-computer',
        noExecutors: 'input[path="/numExecutors"]',
        labels: 'input[path="/labelString"]',
        save: {
            selector: '//button[.="Save"]',
            locateStrategy: 'xpath',
        },
        form: 'form[name="config"]'
    }
};

module.exports.commands = [{
    /**
     * Configure the number of executors we want to assign the node
     *
     * @example configure.setNumber(2);
     * @param newNumber {Number} how many executors we want to assign the node
     */
    setNumber: function (newNumber) {
        const self = this;
        const browser = this.api;
        self.waitForElementPresent('@noExecutors');
        self.clearValue('@noExecutors');
        self.setValue('@noExecutors', newNumber);
        self.clearValue('@labels');// to loose focus on the
        self.waitForElementPresent('@form');
        browser.submitForm('form[name="config"]', function (submitted) {
            self.waitForElementPresent('@computer');
            browser.url(function (response) {
                self.assert.equal(typeof response, "object");
                self.assert.equal(response.status, 0);
                self.assert.equal(response.value.includes('configure'), false);
                return self;
            })

        });
    }

}];
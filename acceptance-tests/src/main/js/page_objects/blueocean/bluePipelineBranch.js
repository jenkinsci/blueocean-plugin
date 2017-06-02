var url = require('../../util/url');
/** @module bluePipelineBranch
 * @memberof page_objects
 * @description Represents the listing of the activities in blueocean
 *
 * @example
 *   // click on the first matching run button (small one)
 browser.page.bluePipelineBranch().clickRunButton();
 * */
module.exports = {
    elements: {
        runButton: 'a.run-button',
        toasts: 'div.toaster div.toast span.text'
    }
};
module.exports.commands = [{
    /**
     * click first run button that can be be identify.
     *
     * @returns {Object} self - nightwatch page object
     */
    clickRunButton: function () {
        const self = this;
        const browser = this.api;
        self.waitForElementVisible('@runButton');
        self.click('@runButton');
        self.waitForElementVisible('@toasts');
        browser.elements('css selector', 'div.toaster div.toast span.text', function (codeCollection) {
            this.assert.equal(codeCollection.value.length, 1);
            codeCollection.value.map(function (item) {
                browser.elementIdText(item.ELEMENT, function (value) {
                    var passed = value.value.indexOf('Queued');
                    if (passed === -1) {
                        passed = value.value.indexOf('Started');
                    }
                    self.assert.equal(passed > -1, true);
                })
            })
        });
        return self;
    }
}];

/** @module util/pageHelper */
module.exports = {
    /**
     Create a callback wrapper - we need to make sure that we have finished before
     we use the callback. If we have an error we invoke with error.
     @param callback the callback we need to call
     */
    createCallbackWrapper: function (callback) {
        return function callbackWrapper(status) {
            if (status && status.state) {
                callback(null, status);
            } else {
                callback(new Error(status))
            }
        };
    },

    /**
     * @description Helper function which validates basic integrity of a nightwatch response object.
     * @param {Object} self - nightwatch page object
     * @param {Object} response - nightwatch response object
     */
    sanityCheck: function (self, response) {
        self.assert.equal(typeof response, "object");
        self.assert.equal(response.status, 0);
    },

    /**
     * @description Helper function which validate that a code block is visible
     * @param {Object} self - nightwatch page object
     * @param {Boolean} [expand=true] - if false we will not validate that a code is present, only that the click was successful.
     * @returns {Function} - callback for onClick validations
     */
    isCodeBlockVisibleCallback: function (self, expand) {
        return function (response) {
            // sanity test
           module.exports.sanityCheck(self, response);
            // default value for expand is true
            if (expand === undefined) {
                expand = true;
            }
            // if we expand, validate that we really did
            if (expand) {
                // we will have a code tag if expand worked
                self.waitForElementVisible('pre')
                    .getText('pre', function (result) {
                        this.assert.notEqual(null, result.value);
                    });
            }
        }
    },
    /**
     * Helper that validates, whether a certain selector return at least one entry
     * @param {String} selector  - a css selector
     * @param {Object} self - nightwatch page object
     * @param {Number} [expectedMinimum] - the minimum which we await
     */
    notEmptyHelper: function (selector, self, expectedMinimum) {
        const browser = self.api;
        browser.elements('css selector', selector, function (codeCollection) {
            module.exports.sanityCheck(self, codeCollection);
            if (expectedMinimum) {
                self.assert.equal(codeCollection.value.length >= expectedMinimum, true);
            } else {
                self.assert.equal(codeCollection.value.length > 0, true);
            }
        });
        return self;
    }
};

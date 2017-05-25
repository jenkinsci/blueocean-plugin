/**
 * @module classicGeneral
 * @memberof page_objects
 * @description Represents a page object containing "general" operations
 * that can be performed on any Jenkins Classic page.
 */

exports.elements = {
    pageBody: '#page-body'
};
exports.commands = [
    {
        /**
         * Navigate to a page.
         * @param {string} pageUrl Relative page URL (relative to nightwatch launchUrl).
         * @returns {Object} self - nightwatch page object
         */
        navigateToRun: function(pageUrl) {
            this.navigate(this.api.launchUrl + '/' + pageUrl);
            this.waitForElementPresent('@pageBody');
            return this;
        }
    }
];
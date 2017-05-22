/**
 * Wait for an element to be visible then click it.
 */
exports.command = function (element) {
    this.waitForElementVisible(element);
    this.click(element);
    return this;
};
exports.command = function (checkfor) {
    this.useXpath().waitForElementVisible("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']");
    this.useXpath().click("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']");
    if (checkfor) {
        this.waitForElementVisible(checkfor);
    }
    return this;
};
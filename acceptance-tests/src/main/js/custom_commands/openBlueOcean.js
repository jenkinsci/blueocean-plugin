exports.command = function (checkfor) {
    this.useXpath().waitForElementVisible("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']").useCss();
    this.useXpath().click("//a[contains(@class, 'task-link') and text()='Open Blue Ocean']").useCss();
    if (checkfor) {
        this.waitForElementVisible(checkfor);
    }
    return this;
};
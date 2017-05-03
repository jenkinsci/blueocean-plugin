exports.command = function (checkfor) {
    this.waitForElementVisible('#open-blueocean-in-context');
    this.click('#open-blueocean-in-context');
    if (checkfor) {
        this.waitForElementVisible(checkfor);
    }
    return this;
};
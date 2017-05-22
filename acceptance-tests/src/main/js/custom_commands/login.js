exports.command = function (checkfor) {
    this.url(this.launchUrl + "/login");
    this.waitForElementVisible('#j_username');
    this.setValue('#j_username', 'alice');
    this.setValue('input[name=j_password', 'alice');
    this.click('#yui-gen1-button');

    return this;
};

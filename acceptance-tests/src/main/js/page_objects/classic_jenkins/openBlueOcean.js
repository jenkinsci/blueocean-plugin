module.exports = {
    elements: {
        openLink: {
            selector: "//a[contains(@class, 'task-link') and text()='Open Blue Ocean']",
            locateStrategy: 'xpath',
        }
    }
};
module.exports.commands = [{

    open: function (checkfor) {
        var self = this;
        self.visible();
        self.click('@openLink');
        if (checkfor) {
            this.waitForElementVisible(checkfor);
        }
        return this;
    },

    visible: function () {
        var self = this;
        self.waitForElementVisible('@openLink');
    }
}];
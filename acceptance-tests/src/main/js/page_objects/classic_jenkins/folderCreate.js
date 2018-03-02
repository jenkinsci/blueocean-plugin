const url = require('../../util/url');
const newJobPageSuffix = 'newJob';
/** @module folderCreate
 * @memberof page_objects
 * @description Represents the folder create page, which is a wrapper around the classic
 * create job page of jenkins.
 *
 * If called as in the example it is the same as
 * ```https://ci.blueocean.io/view/All/newJob```
 *
 * @example
 * const folderCreate = browser.page.folderCreate().navigate();
 * */
module.exports = {
    url: function () {
        return this.api.launchUrl + '/view/All/' + newJobPageSuffix;
    },
    elements: {
        nameInput: '#name',
        folderType: 'li.com_cloudbees_hudson_plugins_folder_Folder',
        freestyleType: 'li.hudson_model_FreeStyleProject',
        newItem: {
            selector: '//a[text()="New Item"]',
            locateStrategy: 'xpath',
        },
        deleteFolder: {
            selector: '//a[text()="Delete Folder"]',
            locateStrategy: 'xpath',
        },
        submit: '#newFormSubmitButtonForATH',
        configForm: 'form[name="config"]',
        configSave: '#newFormSubmitButtonForATH'
    }
};

module.exports.commands = [{
    /**
     * Create a path of folders.
     * @example folderCreate.createFolders(['firstFolder', '三百', 'ñba', '七']);
     * @param folders {Array} an array of {String} representing a deep path
     */
    createFolders: function(folders) {
        var self = this;
        const browser = this.api;
        // we do not want to modify the original array
        const clone = folders.slice();
        const firstChild = clone.shift();

        function createFolder(folderName) {
            self.waitForElementVisible('@folderType');
            self.setValue('@nameInput', folderName);
            self.click('@folderType');
            self.waitForElementVisible('@submit');
            self.click('@submit');

            // We should now be on the config page
            self.waitForElementPresent('@configForm');
            // Need to explicitly move the form buttons because we didn't
            // "navigate" to this page via nightwatch navigate.
            self.moveClassicBottomStickyButtons();
            self.waitForElementVisible('@configSave');
            self.pause(500);
            self.click('@configSave');
            // We know the folder is created once the "Delete Folder"
            // task link is visible
            self.waitForElementVisible('@deleteFolder', function() {});
        }

        createFolder(firstChild);

        // And create the subfolders...
        while(clone.length > 0) {
            var subFolder = clone.shift();
            self.waitForElementVisible('@newItem')
                .click('@newItem');
            // Wait until we're sure we are on the new item page
            self.waitForElementVisible('@folderType');
            // Need to explicitly move the form buttons because we didn't
            // "navigate" to this page via nightwatch navigate.
            self.moveClassicBottomStickyButtons();
            createFolder(subFolder);
        }

        return this;
    },
}];
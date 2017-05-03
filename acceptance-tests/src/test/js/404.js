/** @module 404
 * @memberof testcases
 * @description TEST: we are trying different urls which should result in 404 pages.
 */
const pageHelper = require("../../main/js/util/pageHelper");
const createCallbackWrapper = pageHelper.createCallbackWrapper;
// FIXME: we need to test runs that not yet exist
// add the following as soon we have fixed them
// '/blue/organizations/gibtEsNicht', '/blue/organizations/jenkins/my-pipeline/detail/my-pipeline/20/pipeline'
// test different levels for 404
const urls = ['/blue/gibtEsNicht', '/blue/organizations/jenkins/gibtEsNicht/activity/', '/blue/organizations/gibtEsNicht/gibtEsNicht/detail/gibtEsNicht/'];
module.exports = {

    /**
     * Trying out different urls that should result in the same 404 page
     * @param browser
     */
    'Step 01': function (browser) {
        const url = urls[0];
        console.log('trying url', browser.launchUrl, url);
        // navigate to the url
        browser.url(browser.launchUrl + url, function(result) {
            browser.page.blueNotFound().assertBasicLayoutOkay();
        });
    },
    /**
     * Trying out different urls that should result in the same 404 page
     * @param browser
     */
    'Step 02': function (browser) {
        const url = urls[1];
        console.log('trying url', browser.launchUrl, url);
        // navigate to the url
        browser.url(browser.launchUrl + url, function(result) {
            browser.page.blueNotFound().assertBasicLayoutOkay();
        });
    },
    /**
     * Trying out different urls that should result in the same 404 page
     * @param browser
     */
    'Step 03': function (browser) {
        const url = urls[2];
        console.log('trying url', browser.launchUrl, url);
        // navigate to the url
        browser.url(browser.launchUrl + url, function(result) {
            browser.page.blueNotFound().assertBasicLayoutOkay();
        });
    },
};

/**
 * Checks if the current url ends with the given value.
 *
 * ```
 *    this.demoTest = function (client) {
 *      browser.assert.urlEndsWith('/blue/organizations/jenkins/my-pipeline/activity');
 *    };
 * ```
 *
 * @method urlEndsWith
 * @param {string} expected The expected url.
 * @param {string} [message] Optional log message to display in the output. If missing, one is displayed by default.
 * @api assertions
 */

var util = require('util');
exports.assertion = function(expected, msg) {

    this.message = msg || util.format('Testing if the URL ends with "%s".', expected);
    this.expected = expected;

    this.pass = function(value) {
        if (humanize(value).endsWith(expected)) {
            return true;
        } else {
            console.log("urlEndsWith assert failed. The assert log (see below) shows the dehumanized URLs as seen by the browser. Here's an attempt at decoding/humanizing that URL:");
            console.log("\t" + humanize(value));
            return false;
        }
    };

    this.value = function(result) {
        return result.value;
    };

    this.command = function(callback) {
        this.api.url(callback);
        return this;
    };

};

function humanize(url) {
    var urlTokens = url.split('/');
    var decodedUrlTokens = urlTokens.map(function(token) {
        return decodeURIComponent(token).replace(/\//g, '%2F');
    });

    // pop off the last token if it's empty i.e. there
    // was a trailing slash.
    if (decodedUrlTokens[decodedUrlTokens.length - 1] === '') {
        decodedUrlTokens.pop();
    }

    return decodedUrlTokens.join('/');
}
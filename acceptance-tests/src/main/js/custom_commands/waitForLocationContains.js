/**
 * @module waitForLocationContains
 * @memberof custom_commands
 */
const util = require('util');
const events = require('events');

function Cmd() {
    events.EventEmitter.call(this);
}
util.inherits(Cmd, events.EventEmitter);

var POLLING_FREQUENCY = 100;
var TIMEOUT = 5000;

/**
 * @description Nightwatch command to wait for the browser's href to equal the desired value.
 * Polls the browser at a given frequency to wait for any change to location.href
 */
const waitForLocationContains = function (urlFragment) {
    var self = this;
    var startTime = new Date().getTime();
    var locationPollingTimeout = null;
    var currentUrl = '';

    var checkForUrlChange = function() {
        self.api.url(function (response) {
            currentUrl = response.value;

            if (currentUrl.indexOf(urlFragment) >= 0) {
                var ellapsed = new Date().getTime() - startTime;
                console.log('url matched fragment ' + urlFragment + ' after ' + ellapsed + ' ms; full url=' + currentUrl);
                cleanUp();
                self.emit('complete');
            }
            else {
                locationPollingTimeout = setTimeout(checkForUrlChange, POLLING_FREQUENCY);
            }
        });
    };

    var errorTimeout = setTimeout(function() {
        cleanUp();
        var error = new Error('timed out waiting for url to contain: ' + urlFragment + ', full url=' + currentUrl);
        self.emit('error', error);
    }, TIMEOUT);

    var cleanUp = function() {
        clearTimeout(locationPollingTimeout);
        clearTimeout(errorTimeout);
    };

    checkForUrlChange();

    return this;
};

Cmd.prototype.command = waitForLocationContains;
module.exports = Cmd;
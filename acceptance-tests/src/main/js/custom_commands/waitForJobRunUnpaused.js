/** @module waitForJobRunUnpaused
 * @memberof custom_commands
 * */
const util = require('util');
const events = require('events');
var sseClient = require('../api/sse');

function Cmd() {
    events.EventEmitter.call(this);
}
util.inherits(Cmd, events.EventEmitter);

/**
 * @description Nightwatch command to wait for a job run to end.
 * @param {String} jobName - the name of the job we are waiting on
 * @param {Function} [onBuildUnpaused] - callback to be invoke when unpaused event has arrived,
 *   will pass the sse event to the callback
 * */
const waitForJobRunUnpaused = function (jobName, onBuildUnpaused) {
    var self = this;

    console.log('Waiting for job "' + jobName + '" run to unpause.');
    sseClient.onJobRunUnpaused(jobName, function(event) {
        console.log('Job "' + jobName + '" unpaused.');
        try {
            if (onBuildUnpaused) {
                onBuildUnpaused(event);
            }
        } finally {
            self.emit('complete');
        }
    });

    return this;
};
Cmd.prototype.command = waitForJobRunUnpaused;

module.exports = Cmd;

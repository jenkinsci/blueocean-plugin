/** @module waitForJobRunPaused
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
 * @param {Function} [onBuildPaused] - callback to be invoke when paused event has arrived,
 *   will pass the sse event to the callback
 * */
const waitForJobRunPaused = function (jobName, onBuildPaused) {
    var self = this;

    console.log('Waiting for job "' + jobName + '" run to pause.');
    sseClient.onJobRunPaused(jobName, function(event) {
        console.log('Job "' + jobName + '" paused.');
        try {
            if (onBuildPaused) {
                onBuildPaused(event);
            }
        } finally {
            self.emit('complete');
        }
    });

    return this;
};
Cmd.prototype.command = waitForJobRunPaused;

module.exports = Cmd;
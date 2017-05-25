/** @module waitForJobRunStarted
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
 * @description Nightwatch command to wait for a job run to start.
 * @param {String} jobName - the name of the job we are waiting on
 * @param {Function} [onBuildStarted] - callback to be invoke when finished, will pass the sse event to the callback
 * @param {Number} [timeout] - callback to be invoke when finished, will pass the sse event to the callback
 * */
const waitForJobRunStarted = function (jobName, onBuildStarted, timeout) {
    var self = this;

    var waitTimeout = setTimeout(function() {
        var error = new Error('Timed out waiting for job/pipeline "' + jobName + '" run to start.');
        self.emit('error', error);
    }, (typeof timeout === 'number' ? timeout : 20000));

    console.log('Waiting for job/pipeline "' + jobName + '" run to start.');
    sseClient.onJobRunStarted(jobName, function(event) {
        clearTimeout(waitTimeout);
        console.log('Job/pipeline "' + jobName + '" run started.');
        try {
            if (onBuildStarted) {
                onBuildStarted(event);
            }
        } finally {
            self.emit('complete');
        }
    });

    return this;
};
Cmd.prototype.command = waitForJobRunStarted;

module.exports = Cmd;
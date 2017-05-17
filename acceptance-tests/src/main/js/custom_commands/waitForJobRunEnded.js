/** @module waitForJobRunEnded
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
 * @param {Function} [onBuildComplete] - callback to be invoke when finished, will pass the sse event to the callback
 * */
const waitForJobRunEnded = function (jobName, onBuildComplete) {
    var self = this;

    console.log('Waiting for job "' + jobName + '" run to end.');
    sseClient.onJobRunEnded(jobName, function(event) {
        console.log('Job "' + jobName + '" ended.');
        try {
            if (onBuildComplete) {
                onBuildComplete(event);
            }
        } finally {
            self.emit('complete');
        }
    });

    return this;
};
Cmd.prototype.command = waitForJobRunEnded;

module.exports = Cmd;
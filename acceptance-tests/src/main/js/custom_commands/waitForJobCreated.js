/** @module waitForJobCreated
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
 * @description Nightwatch command to wait for a job to be created.
 * @param {String} jobName - the name of the job we are waiting on
 * @param {Function} [onCreated] - callback to be invoke when finished, will pass the sse event to the callback
 * @param {Number} [timeout] - callback to be invoke when finished, will pass the sse event to the callback
 * */
const waitForJobCreated = function (jobName, onCreated, timeout) {
    var self = this;

    var waitTimeout = setTimeout(function() {
        var error = new Error('Timed out waiting for job "' + jobName + '" to be created. Something must have failed earlier and the job creation did not succeed.');
        self.emit('error', error);
    }, (typeof timeout === 'number' ? timeout : 20000));

    console.log('Waiting on job "' + jobName + '" to be created.');
    sseClient.onJobCreated(jobName, function () {
        clearTimeout(waitTimeout);
        console.log('Job "' + jobName + '" created.');
        try {
            if (onCreated) {
                onCreated(event);
            }
        } finally {
            self.emit('complete');
        }
    });

    return this;
};

Cmd.prototype.command = waitForJobCreated;

module.exports = Cmd;
/** @module waitForJobDeleted
 * @memberof custom_commands
 * */
const util = require('util');
const events = require('events');
var request = require('request');

function Cmd() {
    events.EventEmitter.call(this);
}
util.inherits(Cmd, events.EventEmitter);
/**
 * @description Nightwatch command to wait for a job be deleted.
 * @param {String} jobName - the name of the job we are waiting on
 * @param {Function} [onDeleted] - callback to be invoke when finished
 * */
Cmd.prototype.command = function (jobName, onDeleted) {
    var self = this;

    console.log('Waiting on job "' + jobName + '" to be deleted.');
    var deleteUrl = this.api.launchUrl + '/job/' + jobName + '/doDelete';
    request.post(deleteUrl, function () {
        console.log('Job "' + jobName + '" deleted.');
        try {
            if (onDeleted) {
                onDeleted();
            }
        } finally {
            self.emit('complete');
        }
    });

    return this;
};

module.exports = Cmd;
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
 * @description Nightwatch command to wait for a stage event to be published.
 * @param {String} pipelineName - the name of the job we are waiting on
 * @param {Function} [onPipelineStageEvent] - callback to be invoke when finished, will pass the sse event to the callback
 * */
const waitForPipelineStageEvent = function (pipelineName, onPipelineStageEvent) {
    var self = this;

    console.log('Waiting for pipeline "' + pipelineName + '" stage event.');
    sseClient.onPipelineStage(pipelineName, function(event) {
        console.log('Pipeline "' + pipelineName + '" stage event arrived.');
        try {
            if (onPipelineStageEvent) {
                onPipelineStageEvent(event);
            }
        } finally {
            self.emit('complete');
        }
    });

    return this;
};
Cmd.prototype.command = waitForPipelineStageEvent;

module.exports = Cmd;
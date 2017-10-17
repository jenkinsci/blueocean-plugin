console.log('lu');

import React from 'react';
import { action, observable } from 'mobx';
import { logging, sseConnection } from '@jenkins-cd/blueocean-core-js';
import { KaraokeConfig, KaraokeSpeed } from '../';
import debounce from 'lodash.debounce';

const logger = logging.logger('io.jenkins.blueocean.dashboard.following.liveUpdate');

export class LiveUpdate {
    @observable isActive;

    displayState;
    nodeStore;
    stepStore;

    _updaters = [];
    _logPolling;

    /**
     * @param {function:Promise} updater
     */
    addUpdater(updater) {
        this._updaters.push(updater);
    }

    removeUpdater(updater) {
        if (updater) {
            const idx = this._updaters.indexOf(updater);
            if (idx !== -1) {
                this._updaters = this._updaters.splice(idx, 1);
            }
        } else {
            this._updaters.pop();
        }
    }

    _processUpdates(idx = 0) {
        const updater = this._updaters[idx];
        if (updater) {
            updater().then(() =>
                this._processUpdates(idx + 1)
            );
        }
    }

    constructor(run, displayState, nodeStore, stepStore) {
        this.run = run;
        this.displayState = displayState;
        this.nodeStore = nodeStore;
        this.stepStore = stepStore;
        this.sseEventHandler = this.sseEventHandler.bind(this);
        if (!this.run.isCompleted()) {
            this.setActive(KaraokeConfig.getPreference('runDetails.pipeline.following').value !== 'never');
        }
    }

    start() {
        if (!this.ssePipeline) {
            console.log('starting sse following');
            // get sse listener to react on the different in sse events
            this.ssePipeline = sseConnection.subscribe('pipeline', this.sseEventHandler);
            this.sseJob = sseConnection.subscribe('job', this.sseEventHandler);
            this._logPolling = () => {
                // we need to get more input from the log stream
                console.log('follow along polling mode');
                this._processUpdates();

                setTimeout(() => {
                    if (this._logPolling) {
                        this._logPolling();
                    }
                }, KaraokeSpeed);
            };
            this._logPolling();
        }
    }

    stop() {
        if (this.ssePipeline) {
            console.log('stopping sse following');
            sseConnection.unsubscribe(this.ssePipeline);
            delete this.ssePipeline;
            sseConnection.unsubscribe(this.sseJob);
            delete this.sseJob;
            delete this._logPolling;
        }
    }

    cleanup() {
        this.stop();
    }
    /**
     * Listen for pipeline flow node events. We need to re-fetch in case of some events.
     * @param event sse event coming from the back-end
     */
    sseEventHandler(event) {
        console.log('event.jenkins_event', event.jenkins_event);

         // we are using try/catch to throw an early out error
        try {
            const followingDisabled = !this.isActive;
            const jenkinsEvent = event.jenkins_event;
            const runId = this.run.id;
             // we get events from the pipeline and the job channel, they have different naming for the id
            //  && event.jenkins_object_id !== runId -> job
            if (event.pipeline_run_id !== runId) {
                logger.debug('ignoring event for different run');
                return;
            }
            switch (jenkinsEvent) {
            case 'pipeline_step': {
                if (followingDisabled) {
                    logger.debug('early out because we do not want to follow along sse events');
                    return;
                }
                logger.debug('sse event step fetchCurrentSteps', jenkinsEvent);
                this.stepStore.fetch();
                // prevent flashing of stages and nodes
                this.showPending = false;
                break;
            }
            case 'pipeline_end':
            case 'pipeline_start':
            case 'job_run_ended':
            case 'pipeline_block_start':
            case 'pipeline_stage': {
                logger.debug('sse event block starts refetchNodes', jenkinsEvent);
                this.nodeStore.fetch()
                    .then(() => this.displayState.setCurrentNode())
                    .then(() => this.stepStore.fetch());
                if (jenkinsEvent === 'pipeline_end') {
                    this.setActive(false);
                }
                // prevent flashing of stages and nodes
                this.showPending = false;
                break;
            }
            case 'pipeline_block_end': {
                console.log('pipeline block end', this.displayState.currentNode, 'isRunning?', this.displayState.currentNode.isRunning);
                this.nodeStore.fetch()
                    .then(data => {
                        if (!this.displayState.currentNode.isRunning) {
                            this.transitionNode();
                        }
                        return data;
                    });
                break;
            }
            default: {
                logger.debug('unhandled event', jenkinsEvent, 'from sse message', event);
            }
            }
        } catch (e) {
            logger.error('sse Event has produced an error, will not work as expected.', e);
        }
    }

    transitionNode = debounce(action(() => {
        console.log('transitioning to next running node', this.displayState.currentNode);
        // short delay before transition handled by debounce
        for (const node of this.nodeStore.nodes.data.model) {
            if (node.isRunning) {
                this.stepStore.fetch()
                    .then(() => this.displayState.setCurrentNode(node.id))
                    .then(() => {
                        this.stepStore.url = this.displayState.stepsUrl;
                        this.stepStore.fetch();
                    });
                return;
            }
        }
    }), KaraokeSpeed);

    @action
    setActive(isActive) {
        if (!this.isActive && isActive) {
            // make sure this is listening for SSE events if it's active, this will be a noop if already listening
            this.start();
        }
        this.isActive = isActive;
    }
}

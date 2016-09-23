/**
 * Created by cmeyers on 7/29/16.
 */
import config from '../urlconfig';
import utils from '../utils';
import QueueUtils from '../QueueUtils';

/**
 * Wraps the SSE Gateway and fetches data related to events from REST API.
 */
export class SseBus {

    constructor(sse, rest) {
        this.id = this._random();
        this.sse = sse;
        this.pipelineApi = rest.pipelineApi;
        this.runApi = rest.runApi;
        this.queueApi = rest.queueApi;
        this.sseConnected = false;
        this.externalListeners = {};
        this.sseListeners = {};
    }

    dispose() {
        Object.keys(this.sseListeners).forEach((token) => {
            this.unsubscribe(token);
        });

        this.externalListeners = {};
        this.sseListeners = {};
    }

    /**
     * Subscribe to job events.
     * @param callback func to invoke with job data
     * @param jobFilter func invoked for each job event, return false to suppress callback invocation
     * @returns {number} unsubscribe token
     */
    subscribeToJob(callback, jobFilter) {
        this._initialize();

        const id = this._random();

        this.externalListeners[id] = {
            listener: callback,
            filter: jobFilter,
        };

        if (!this.sseListeners.job) {
            const sseListener = this.sse.subscribe('job', (event) => {
                this._handleJobEvent(event);
            });

            this.sseListeners.job = sseListener;
        }

        return id;
    }

    unsubscribe(token) {
        delete this.externalListeners[token];

        if (Object.keys(this.externalListeners).length === 0) {
            this.sse.unsubscribe(this.sseListeners.job);
            delete this.sseListeners.job;
        }
    }

    _initialize() {
        if (!this.sseConnected) {
            // FIXME sse should not require this to end with a /
            this.sse.connect({
                clientId: 'jenkins-blueocean-core-js',
                onConnect: undefined,
                jenkinsUrl: `${config.getJenkinsRootURL()}/`,
            });

            this.sseConnected = true;
        }
    }

    _handleJobEvent(event) {
        const subscriptions = Object
            .keys(this.externalListeners)
            .map(subId => this.externalListeners[subId]);

        const interestedListeners = subscriptions
            .filter(sub => sub.filter(event))
            .map(sub => sub.listener);

        // if no filters are interested in the event, bail
        if (interestedListeners.length === 0) {
            return;
        }

        switch (event.jenkins_event) {
        case 'job_crud_created':
        case 'job_crud_deleted':
        case 'job_crud_renamed':
            this._refetchPipelines();
            break;
        case 'job_run_queue_enter':
            this._enqueueJob(event, interestedListeners);
            break;
        case 'job_run_queue_buildable':
            break;
        case 'job_run_queue_left':
            this._unEnqueueJob(event, interestedListeners);
            break;
        case 'job_run_queue_blocked': {
            break;
        }
        case 'job_run_started': {
            this._updateJob(event, interestedListeners);
            break;
        }
        case 'job_run_ended': {
            this._updateJob(event, interestedListeners);
            break;
        }
        default :
        // Else ignore the event.
        }
    }

    _refetchPipelines() {
        // TODO: implement once migration into commons JS
    }

    _enqueueJob(event, listeners) {
        // try to fetch from the queue and swallow any error
        // if it 404's, then very likely it's already started running
        // that SSE will trigger 'updateJob' and update it to proper status
        this.queueApi.fetchQueueItemFromEvent(event)
            .then(data => {
                const pseudoRun = QueueUtils.mapQueueImplToPseudoRun(data);

                for (const listener of listeners) {
                    listener(pseudoRun, event);
                }
            })
            .catch(() => {});
    }

    _updateJob(event, listeners) {
        this.runApi.fetchRunFromEvent(event)
            .then(data => {
                const updatedRun = utils.clone(data);

                // in many cases the SSE and subsequent REST call occur so quickly
                // that the run's state is stale. force the state to the correct value.
                if (event.jenkins_event === 'job_run_ended') {
                    updatedRun.state = 'FINISHED';
                } else {
                    updatedRun.state = 'RUNNING';
                }

                for (const listener of listeners) {
                    listener(updatedRun, event);
                }
            });
    }

    // if the job was removed / canceled from the queue, then just fetch the latest run
    // and use that to update the pipeline's status
    _unEnqueueJob(event, listeners) {
        this.pipelineApi.fetchLatestRun(event.blueocean_job_rest_url)
            .then(data => {
                for (const listener of listeners) {
                    listener(data, event);
                }
            });
    }

    _updateMultiBranchPipelineBranches() {
        // TODO: implement once migration into commons JS
    }

    /**
     * @returns {number}
     * @private
     */
    _random() {
        return Math.random() * Math.pow(10, 16);
    }
}

/**
 * Created by cmeyers on 7/29/16.
 */
import defaultFetch from 'isomorphic-fetch';
import config from '../urlconfig';
import utils from '../utils';

/**
 * Wraps the SSE Gateway and fetches data related to events from REST API.
 */
export class SseBus {

    constructor(connection, fetch) {
        this.id = this._random();
        this.connection = connection;
        this.fetch = fetch || defaultFetch;
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
        const id = this._random();

        this.externalListeners[id] = {
            listener: callback,
            filter: jobFilter,
        };

        if (!this.sseListeners.job) {
            const sseListener = this.connection.subscribe('job', (event) => {
                this._handleJobEvent(event);
            });

            this.sseListeners.job = sseListener;
        }

        return id;
    }

    unsubscribe(token) {
        delete this.externalListeners[token];

        if (Object.keys(this.externalListeners).length === 0) {
            this.connection.unsubscribe(this.sseListeners.job);
            delete this.sseListeners.job;
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
        case 'job_run_queue_buildable':
        case 'job_run_queue_enter':
            this._enqueueJob(event, interestedListeners);
            break;
        case 'job_run_queue_left':
        case 'job_run_queue_blocked': {
            break;
        }
        case 'job_run_paused':
        case 'job_run_unpaused':
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
        const queuedRun = {};

        queuedRun.pipeline = event.job_ismultibranch ?
            event.blueocean_job_branch_name :
            event.blueocean_job_pipeline_name;

        const runUrl = utils.cleanSlashes(`${event.blueocean_job_rest_url}/runs/${event.job_run_queueId}`);

        queuedRun._links = {
            self: {
                href: runUrl,
            },
        };

        queuedRun.state = 'QUEUED';
        queuedRun.result = 'UNKNOWN';

        for (const listener of listeners) {
            listener(queuedRun, event);
        }
    }

    _updateJob(event, listeners) {
        const baseUrl = config.getJenkinsRootURL();
        const url = utils.cleanSlashes(`${baseUrl}/${event.blueocean_job_rest_url}/runs/${event.jenkins_object_id}`);

        this.fetch(url)
            .then((data) => {
                const updatedRun = utils.clone(data);

                // FIXME: Talk to CMeyers why we cannot use the data.state?
                // in many cases the SSE and subsequent REST call occur so quickly
                // that the run's state is stale. force the state to the correct value.
                if (event.jenkins_event === 'job_run_ended') {
                    updatedRun.state = 'FINISHED';
                } else if (event.jenkins_event === 'job_run_paused') {
                    updatedRun.state = 'PAUSED';
                } else {
                    updatedRun.state = 'RUNNING';
                }

                for (const listener of listeners) {
                    listener(updatedRun, event);
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

/**
 * Created by cmeyers on 7/29/16.
 */
import defaultFetch from 'isomorphic-fetch';

// TODO: all these utilities can be removed once merged with new JWT / fetch code
import { cleanSlashes } from './UrlUtils';
import urlConfig from './config';
urlConfig.loadConfig();

const defaultFetchOptions = {
    credentials: 'same-origin',
};

// TODO: remove all this code once JWT Fetch is integration
function checkStatus(response) {
    if (response.status >= 300 || response.status < 200) {
        const error = new Error(response.statusText);
        error.response = response;
        throw error;
    }
    return response;
}

function parseJSON(response) {
    return response.json()
    // FIXME: workaround for status=200 w/ empty response body that causes error in Chrome
    // server should probably return HTTP 204 instead
        .catch((error) => {
            if (error.message === 'Unexpected end of JSON input') {
                return {};
            }
            throw error;
        });
}

function clone(json) {
    return JSON.parse(JSON.stringify(json));
}

/**
 * Wraps the SSE Gateway and fetches data related to events from REST API.
 */
export class SseBus {

    constructor(sse, fetch) {
        this.id = this._random();
        this.sse = sse;
        this.fetch = fetch || defaultFetch;
        this.sseConnected = false;
        this.externalListeners = {};
        this.sseListeners = {};
    }

    dispose() {
        for (const token in Object.keys(this.sseListeners)) {
            this.unsubscribe(token);
        }

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

        if (!this.sseListeners['job']) {
            const sseListener = this.sse.subscribe('job', (event) => {
                this._handleJobEvent(event);
            });

            this.sseListeners['job'] = sseListener;
        }

        return id;
    }

    unsubscribe(token) {
        delete this.externalListeners[token];

        if (Object.keys(this.externalListeners).length === 0) {
            this.sse.unsubscribe(this.sseListeners['job']);
            delete this.sseListeners['job'];
        }
    }

    _initialize() {
        if (!this.sseConnected) {
            // FIXME sse should not require this to end with a /
            this.sse.connect({
                clientId: 'jenkins-blueocean-core-js',
                onConnect: undefined,
                jenkinsUrl: `${urlConfig.jenkinsRootURL}/`,
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
            case 'job_run_queue_buildable':
            case 'job_run_queue_enter':
                this._enqueueJob(event, interestedListeners);
                break;
            case 'job_run_queue_left':
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
        const queuedRun = {};

        queuedRun.pipeline = event.job_ismultibranch ?
            event.blueocean_job_branch_name :
            event.blueocean_job_pipeline_name;

        const runUrl = cleanSlashes(`${event.blueocean_job_rest_url}/runs/${event.job_run_queueId}`);

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
        const baseUrl = urlConfig.jenkinsRootURL;
        const url = cleanSlashes(`${baseUrl}/${event.blueocean_job_rest_url}/runs/${event.jenkins_object_id}`);

        this.fetch(url, defaultFetchOptions)
            .then(checkStatus)
            .then(parseJSON)
            .then((data) => {
                const updatedRun = clone(data);

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

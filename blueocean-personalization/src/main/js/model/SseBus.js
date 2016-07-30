/**
 * Created by cmeyers on 7/29/16.
 */
import defaultFetch from 'isomorphic-fetch';

/**
 * Wraps the SSE Gateway and fetches data related to events from REST API.
 * TODO: should probably send additional data *and* the original event to callback
 */
export class SseBus {

    constructor(sse, fetch) {
        this.sse = sse;
        this.fetch = fetch || defaultFetch;
        this.jobListenerSse = null;
        this.jobListenerExternal = null;
    }

    dispose() {
        if (this.jobListenerSse) {
            this.sse.unsubscribe(this.jobListenerSse);
            this.jobListenerSse = null;
        }

        if (this.jobListenerExternal) {
            this.jobListenerExternal = null;
        }
    }

    subscribeToJob(callback) {
        console.log('subscribeToJob with ', callback);
        this.jobListenerExternal = callback;
        this.jobListenerSse = this.sse.subscribe('job', (event) => {
            this._handleJobEvent(event);
        });
    }

    _handleJobEvent(event) {
        switch (event.jenkins_event) {
            case 'job_crud_created':
            case 'job_crud_deleted':
            case 'job_crud_renamed':
                this._refetchPipelines();
                break;
            case 'job_run_queue_buildable':
            case 'job_run_queue_enter':
                this._enqueueJob(event);
                break;
            case 'job_run_queue_left':
            case 'job_run_queue_blocked': {
                break;
            }
            case 'job_run_started': {
               this._updateJob(event);
                break;
            }
            case 'job_run_ended': {
                this._updateJob(event);
                break;
            }
            default :
            // Else ignore the event.
        }
    }

    _refetchPipelines() {
        // TODO: implement once migration into commons JS
    }

    _enqueueJob(event) {
        const newRun = {
            event,
        };

        newRun.pipeline = event.job_ismultibranch ?
            event.blueocean_job_branch_name :
            event.blueocean_job_pipeline_name;

        const baseUrl = '/blue';
        const runUrl = cleanSlashes(`${baseUrl}/${event.blueocean_job_rest_url}/runs/${event.job_run_queueId}`);

        newRun._links = {
            self: {
                href: runUrl,
            },
        };

        newRun.state = 'QUEUED';
        newRun.result = 'UNKNOWN';

        console.log('enqueueJob', event);

        if (this.jobListenerExternal) {
            this.jobListenerExternal(newRun);
        }
    }

    _updateJob(event) {
        const baseUrl = '/jenkins/blue';
        const url = cleanSlashes(`${baseUrl}/${event.blueocean_job_rest_url}/runs/${event.jenkins_object_id}`);

        this.fetch(url)
            .then(checkStatus)
            .then(parseJSON)
            .then((data) => {
                console.log('updateJob', event, data);

                if (event.jenkins_event === 'job_run_ended') {
                    data.state = 'FINISHED';
                } else {
                    data.state = 'RUNNING';
                }

                if (this.jobListenerExternal) {
                    this.jobListenerExternal(data);
                }
            });
    }

    _updateMultiBranchPipelineBranches() {
        // TODO: implement once migration into commons JS
    }


}

// TODO: migrate all this code down to 'fetch'
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

export const cleanSlashes = (url) => {
    if (url.indexOf('//') !== -1) {
        let cleanUrl = url.replace('//', '/');
        cleanUrl = cleanUrl.substr(-1) === '/' ?
            cleanUrl : `${cleanUrl}/`;

        return cleanSlashes(cleanUrl);
    }

    return url;
};

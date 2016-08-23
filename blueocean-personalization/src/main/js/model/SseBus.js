/**
 * Created by cmeyers on 7/29/16.
 */

import { FetchUtils, UrlUtils } from '@jenkins-cd/blueocean-core-js';
import { cleanSlashes } from '../util/UrlUtils';

function clone(json) {
    return JSON.parse(JSON.stringify(json));
}

/**
 * Wraps the SSE Gateway and fetches data related to events from REST API.
 */
export class SseBus {

    constructor(sse) {
        this.sse = sse;
        this.jobListenerSse = null;
        this.jobListenerExternal = null;
        this.jobFilter = null;
    }

    dispose() {
        if (this.jobListenerSse) {
            this.sse.unsubscribe(this.jobListenerSse);
            this.jobListenerSse = null;
        }

        if (this.jobListenerExternal) {
            this.jobListenerExternal = null;
        }

        if (this.jobFilter) {
            this.jobFilter = null;
        }
    }

    /**
     * Subscribe to job events.
     * @param callback func to invoke with job data
     * @param jobFilter func invoked for each job event, return false to suppress callback invocation
     */
    subscribeToJob(callback, jobFilter) {
        this.jobListenerExternal = callback;
        this.jobListenerSse = this.sse.subscribe('job', (event) => {
            this._handleJobEvent(event);
        });
        this.jobFilter = jobFilter;
    }

    _handleJobEvent(event) {
        // if the filter is not interested in the event, bail
        if (this.jobFilter && !this.jobFilter(event)) {
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
        const queuedRun = {
            event,
        };

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

        if (this.jobListenerExternal) {
            this.jobListenerExternal(queuedRun);
        }
    }

    _updateJob(event) {
        const baseUrl = UrlUtils.getJenkinsRootURL();
        const url = cleanSlashes(`${baseUrl}/${event.blueocean_job_rest_url}/runs/${event.jenkins_object_id}`);

        FetchUtils.fetchJson(url)
            .then((data) => {
                const updatedRun = clone(data);

                // in many cases the SSE and subsequent REST call occur so quickly
                // that the run's state is stale. force the state to the correct value.
                if (event.jenkins_event === 'job_run_ended') {
                    updatedRun.state = 'FINISHED';
                } else {
                    updatedRun.state = 'RUNNING';
                }

                if (this.jobListenerExternal) {
                    this.jobListenerExternal(updatedRun);
                }
            }).catch(FetchUtils.consoleError);
    }

    _updateMultiBranchPipelineBranches() {
        // TODO: implement once migration into commons JS
    }
}

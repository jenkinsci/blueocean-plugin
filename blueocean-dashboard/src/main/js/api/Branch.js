/**
 * Simple pipeline branch API component.
 * <p>
 * Non-react component that contains general API methods for
 * interacting with pipeline branches, encapsulating REST API calls etc.
 */

import fetch from 'isomorphic-fetch';
import config from '../config';
import Pipeline from './Pipeline';
import * as urlUtils from '../util/UrlUtils';
import * as sse from '@jenkins-cd/sse-gateway';

export default class Branch {

    constructor(pipeline, name, url) {
        this.pipeline = pipeline;
        this.name = name;
        this.url = url;
        if (!this.url) {
            this.url = `/rest/organizations/${this.pipeline.organization}/pipelines/${this.pipeline.name}/branches/${this.name}`;
        }

        this.sseListeners = [];
    }

    runDetailsRouteUrl(runId) {
        if (runId === undefined) {
            throw new Error('Branch.runDetailsRouteUrl must be supplied with a "runId" parameter.');
        }
        return urlUtils.buildRunDetailsUrl(
            this.pipeline.organization,
            this.pipeline.name,
            this.name, runId, 'pipeline');
    }

    restUrl() {
        return `${config.blueoceanAppURL}${this.url}`;
    }

    onJobChannelEvent(callback) {
        const _this = this;
        const jobListener = sse.subscribe('job', (event) => {
            const eventBranch = exports.fromSSEEvent(event);
            if (_this.equals(eventBranch)) {
                callback(event);
            }
        });

        this.sseListeners.push(jobListener);
    }

    clearEventListeners() {
        for (let i = 0; i < this.sseListeners.length; i++) {
            try {
                sse.unsubscribe(this.sseListeners[i]);
            } catch (e) {
                console.error('Unexpected error clearing SSE event listeners from Branch object');
                console.error(e);
            }
        }
        this.sseListeners = [];
    }

    run(onSuccess, onFail) {
        const url = `${this.restUrl()}/runs/`;

        fetch(url, {
            method: 'post',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
            },
        }).then((response) => {
            if (onSuccess && response.status >= 200 && response.status < 300) {
                onSuccess(response);
            } else if (onFail && (response.status < 200 || response.status > 299)) {
                onFail(response);
            }
        });
    }

    equals(branch) {
        if (branch && branch.name === this.name) {
            // and it's the same pipeline...
            return (
                branch.pipeline.organization === this.pipeline.organization &&
                branch.pipeline.name === this.pipeline.name
            );
        }
        return false;
    }
}

exports.fromSSEEvent = function (event) {
    if (event.job_ismultibranch === undefined || event.job_ismultibranch !== 'true') {
        return undefined;
    }
    return new Branch(
        new Pipeline(event.jenkins_org, event.blueocean_job_pipeline_name),
        event.blueocean_job_branch_name, event.blueocean_job_rest_url
    );
};

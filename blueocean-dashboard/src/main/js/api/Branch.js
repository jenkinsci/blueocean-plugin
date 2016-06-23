/**
 * Simple pipeline branch API component.
 * <p>
 * Non-react component that contains general API methods for
 * interacting with pipeline branches, encapsulating REST API calls etc.
 */

import fetch from 'isomorphic-fetch';
import config from '../config';
import Pipeline from './Pipeline';
import * as sse from '@jenkins-cd/sse-gateway';
import * as pushEventUtil from '../util/push-event-util';

export default class Branch {

    constructor(pipeline, name) {
        this.pipeline = pipeline;
        this.name = name;
        this.sseListeners = [];
    }

    runDetailsRouteUrl(runId) {
        if (runId === undefined) {
            throw new Error('Branch.runDetailsRouteUrl must be supplied with a "runId" parameter.');
        }
        return `/organizations/${this.pipeline.organization}/${this.pipeline.name}/detail/${this.name}/${runId}/pipeline`;
    }

    restUrl() {
        return `${config.blueoceanAppURL}/rest/organizations/${this.pipeline.organization}/pipelines/${this.pipeline.name}/branches/${this.name}`;
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

    run(onFail) {
        const url = `${this.restUrl()}/runs/`;

        fetch(url, {
            method: 'post',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
            },
        }).then((response) => {
            if (onFail && (response.status < 200 || response.status > 299)) {
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
    const eventCopy = pushEventUtil.enrichJobEvent(event);
    if (!eventCopy.blueocean_is_multi_branch) {
        return undefined;
    }
    return new Branch(
        new Pipeline('jenkins', eventCopy.blueocean_job_name),
        eventCopy.blueocean_branch_name
    );
};

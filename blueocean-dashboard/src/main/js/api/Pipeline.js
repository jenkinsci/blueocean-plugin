/**
 * Simple pipeline API component.
 * <p>
 * Non-react component that contains general API methods for
 * interacting with pipelines, encapsulating REST API calls etc.
 */

import fetch from 'isomorphic-fetch';
import config from '../config';
import * as urlUtils from '../util/UrlUtils';
import * as sse from '@jenkins-cd/sse-gateway';
import assert from 'assert';

const MULTI_BRANCH_PIPELINE_CLASS_NAME = 'io.jenkins.blueocean.rest.impl.pipeline.MultiBranchPipelineImpl';

const TYPE = 'Pipeline';
export default class Pipeline {

    constructor(organization, pipelineName, url) {
        assert(typeof organization === 'string', '"organization" must be a string');
        assert(typeof pipelineName === 'string', '"pipelineName" must be a string');

        this._type = TYPE;
        this.organization = organization;
        this.pipelineName = pipelineName;
        this.branchName = pipelineName;
        this.url = url;
        if (!url) {
            this.url = `/rest/organizations/${this.organization}/pipelines/${this.pipelineName}`;
        }

        this.sseListeners = [];
    }

    runDetailsRouteUrl(runId) {
        if (runId === undefined) {
            throw new Error('Branch.runDetailsRouteUrl must be supplied with a "runId" parameter.');
        }

        // clean the branch name before passing it through to URL builder
        const branchName = Pipeline.cleanBranchName(this.branchName);

        return urlUtils.buildRunDetailsUrl(
            this.organization,
            this.pipelineName,
            branchName, runId, 'pipeline');
    }

    restUrl() {
        return `${config.blueoceanAppURL}${this.url}`;
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

    onJobChannelEvent(callback) {
        const _this = this;
        const jobListener = sse.subscribe('job', (event) => {
            const eventBranch = Pipeline.fromSSEEvent(event);
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
                console.error(`Unexpected error clearing SSE event listeners from ${typeof this} object`);
                console.error(e);
            }
        }
        this.sseListeners = [];
    }

    equals(pipeline) {
        if (pipeline && pipeline._type === TYPE) {
            return (
                pipeline.organization === this.organization &&
                pipeline.pipelineName === this.pipelineName
            );
        }
        return false;
    }

    /**
     * Strip an encoded branch name down to its fully unencoded form
     * @param {string} branchName
     * @returns {string}
     */
    static cleanBranchName(branchName) {
        let branch = branchName;
        while (branch !== decodeURIComponent(branch)) {
            branch = decodeURIComponent(branch);
        }
        return branch;
    }
}

/**
 * Test if a pipeline is multi-branch or not.
 * @param pipeline Pipeline JSON object from the REST API, or just the '_class'
 * value from the same object.
 * @returns {boolean} True if the pipeline is a multi-branch pipeline, otherwise false.
 */
Pipeline.isMultibranch = function (pipeline) {
    // TODO: Use the new classes rest API to determine this ala https://issues.jenkins-ci.org/browse/JENKINS-36388
    if (typeof pipeline === 'string') {
        return (pipeline === MULTI_BRANCH_PIPELINE_CLASS_NAME);
    } else if (typeof pipeline === 'object' && pipeline._class) {
        return (pipeline._class === MULTI_BRANCH_PIPELINE_CLASS_NAME);
    }
    console.error(new Error('Illegal argument. The "pipeline" argument must be a string, or a REST response object with a "_class" property.'));
    return false;
};


Pipeline.fromSSEEvent = function (event) {
    // Using require Vs import because Branch extends Pipeline and
    // importing causes babel to balk with an incomprehensible error.
    const Branch = require('./Branch').default;

    if (event.job_ismultibranch && event.job_ismultibranch === 'true') {
        return new Branch(event.jenkins_org, event.blueocean_job_pipeline_name,
            event.blueocean_job_branch_name, event.blueocean_job_rest_url
        );
    }
    return new Pipeline(event.jenkins_org, event.blueocean_job_pipeline_name,
        event.blueocean_job_rest_url);
};

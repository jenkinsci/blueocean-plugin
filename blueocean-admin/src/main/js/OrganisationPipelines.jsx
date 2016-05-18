import React, { Component, PropTypes } from 'react';
import { actions, pipelines as pipelinesSelector, connect, createSelector } from './redux';
import * as sse from '@jenkins-cd/sse-gateway';

const { object, array, func, node } = PropTypes;

class OrganisationPipelines extends Component {

    getChildContext() {
        const {
            params,
            location,
            pipelines,
        } = this.props;

        // The specific pipeline we may be focused on
        let pipeline;

        if (pipelines && params && params.pipeline) {
            const name = params.pipeline;
            pipeline = pipelines.find(aPipeLine => aPipeLine.name === name);
        }

        return {
            pipelines,
            pipeline,
            params,
            location,
        };
    }

    componentWillMount() {
        if (this.context.config) {
            this.props.fetchPipelinesIfNeeded(this.context.config);
            const _this = this;

            // Subscribe for job channel push events
            this.jobListener = sse.subscribe('job', (event) => {
                // Enrich the event with blueocean specific properties
                // before passing it on to be processed.

                const eventCopy = Object.assign({}, event);

                // For blueocean, we split apart the Job name and URL to get the
                // parts needed for looking up the correct pipeline, branch
                // and run etc.
                // TODO: what about nested folders ?
                const jobURLTokens = event.jenkins_object_url.split('/');
                const jobNameTokens = event.job_name.split('/');
                if (jobURLTokens[jobURLTokens.length - 1] === '') {
                    // last token can be an empty string if the url has a trailing slash
                    jobURLTokens.pop();
                }
                if (!isNaN(jobURLTokens[jobURLTokens.length - 1])) {
                    // last/next-last token is a number (a build/run number)
                    jobURLTokens.pop();
                }
                if (jobURLTokens.length > 3
                    && jobURLTokens[jobURLTokens.length - 2] === 'branch') {
                    // So it's a multibranch. The URL looks something like
                    // "job/CloudBeers/job/PR-demo/branch/quicker/".
                    // But we extract the job and branch name from event.job_name.
                    eventCopy.blueocean_branch_name = jobNameTokens.pop();
                    eventCopy.blueocean_is_multi_branch = true;
                    eventCopy.blueocean_job_name = jobNameTokens.pop();
                } else {
                    // It's not multibranch ... 1st token is the pipeline (job) name.
                    // But we extract the job name from event.job_name.
                    eventCopy.blueocean_job_name = jobNameTokens.pop();
                    eventCopy.blueocean_is_multi_branch = false;
                }

                // Is this even associated with the currently active pipeline job?
                eventCopy.blueocean_is_for_current_job =
                    (eventCopy.blueocean_job_name === _this.props.params.pipeline);

                // See http://tfennelly.github.io/jenkins-pubsub-light-module/org/jenkins/pubsub/Events.JobChannel.html
                switch (eventCopy.jenkins_event) {
                case 'job_run_queue_buildable':
                case 'job_run_queue_enter':
                    _this.props.processJobQueuedEvent(eventCopy);
                    break;
                case 'job_run_queue_left':
                case 'job_run_queue_blocked': {
                    break;
                }
                case 'job_run_started': {
                    _this.props.updateRunState(eventCopy, _this.context.config, true);
                    break;
                }
                case 'job_run_ended': {
                    _this.props.updateRunState(eventCopy, _this.context.config);
                    break;
                }
                default :
                    // Else ignore the event.
                }
            });
        }
    }
    componentWillUnmount() {
        if (this.jobListener) {
            sse.unsubscribe(this.jobListener);
            delete this.jobListener;
        }
    }

    render() {
        return this.props.children; // Set by router
    }
}

OrganisationPipelines.contextTypes = {
    router: object.isRequired,
    config: object.isRequired,
};

OrganisationPipelines.propTypes = {
    fetchPipelinesIfNeeded: func.isRequired,
    processJobQueuedEvent: func.isRequired,
    updateRunState: func.isRequired,
    params: object, // From react-router
    children: node, // From react-router
    location: object, // From react-router
    pipelines: array,
};

OrganisationPipelines.childContextTypes = {
    pipelines: array,
    pipeline: object,
    params: object, // From react-router
    location: object, // From react-router
};

const selectors = createSelector([pipelinesSelector], (pipelines) => ({ pipelines }));

export default connect(selectors, actions)(OrganisationPipelines);

import React, { Component, PropTypes } from 'react';
import {
    actions,
    pipelines as pipelinesSelector,
    isMultiBranch as isMultiBranchSelector,
    connect,
    createSelector,
} from './redux';
import * as sse from '@jenkins-cd/sse-gateway';
import * as pushEventUtil from './util/push-event-util';

const { object, array, func, node } = PropTypes;

// Connect to the SSE Gateway and allocate a
// dispatcher for blueocean.
// TODO: We might want to move this code to a local SSE util module.
sse.connect('jenkins_blueocean');

class OrganisationPipelines extends Component {

    // FIXME: IMO the following should be droped
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
        const config = this.context.config;
        if (config) {
            this.props.fetchPipelinesIfNeeded(config);
            if (this.props.params.pipeline) {
                const { pipeline } = this.props.params;
                config.pipeline = pipeline;
                this.props.setPipeline(config);
            }
            const _this = this;

            // Subscribe for job channel push events
            this.jobListener = sse.subscribe('job', (event) => {
                // Enrich the event with blueocean specific properties
                // before passing it on to be processed.

                const eventCopy = pushEventUtil.enrichJobEvent(event, _this.props.params.pipeline);

                // See http://jenkinsci.github.io/pubsub-light-module/org/jenkins/pubsub/Events.JobChannel.html
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


    componentWillReceiveProps(nextProps) {
        if (nextProps.params.pipeline !== this.props.params.pipeline) {
            const config = this.context.config;
            const { pipeline } = nextProps.params;
            config.pipeline = pipeline;
            this.props.setPipeline(config);
        }
    }

    componentWillUnmount() {
        if (this.jobListener) {
            sse.unsubscribe(this.jobListener);
            delete this.jobListener;
        }
    }
    /*
     FIXME we should use clone here, this way we could pass all actions and reducer down to all
     components and get rid of the seperate connect in each subcomponents -> see RunDetailsPipeline
     */
    render() {
        return this.props.children;
    }
}

OrganisationPipelines.contextTypes = {
    router: object.isRequired,
    config: object.isRequired,
};

OrganisationPipelines.propTypes = {
    fetchPipelinesIfNeeded: func.isRequired,
    setPipeline: func,
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

const selectors = createSelector([pipelinesSelector, isMultiBranchSelector],
    (pipelines, isMultiBranch) => ({ pipelines, isMultiBranch }));

export default connect(selectors, actions)(OrganisationPipelines);

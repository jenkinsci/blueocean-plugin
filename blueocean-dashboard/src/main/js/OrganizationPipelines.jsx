import React, { Component, PropTypes } from 'react';
import {
    actions,
    pipelines as pipelinesSelector,
    connect,
    createSelector,
} from './redux';
import * as sse from '@jenkins-cd/sse-gateway';
import * as pushEventUtil from './util/push-event-util';

const { object, array, func, node, string } = PropTypes;

// Connect to the SSE Gateway and allocate a
// dispatcher for blueocean.
// TODO: We might want to move this code to a local SSE util module.
sse.connect('jenkins_blueocean');

class OrganizationPipelines extends Component {

    // FIXME: IMO the following should be dropped
    getChildContext() {
        const {
            params,
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
        };
    }

    componentWillMount() {
        const config = this.context.config;
        if (config) {
            const { organization } = this.context.params;
            this.props.fetchPipelinesIfNeeded(this.context.config, organization);
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
                case 'job_crud_created':
                case 'job_crud_deleted':
                case 'job_crud_renamed':
                    // Just refetch and update the pipelines list.
                    // Yes, in some of these cases it would be possible to
                    // update the redux store state without making a REST call.
                    // Trading off for simplicity and view consistency here.
                    // Doing it this way leaves the code a lot simpler + guarantees
                    // That the user sees the pipelines in the same order etc as they
                    // would if they did a page reload. Also remember that these
                    // crud operations are relative low frequency, so not much
                    // benefit to be got from optimizing things here.
                    // TODO: fix https://issues.jenkins-ci.org/browse/JENKINS-35153 for delete
                    _this.props.fetchPipelines(_this.context.config, _this.props.organization);
                    break;
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
                    _this.props.updateBranchState(eventCopy, _this.context.config);
                    break;
                }
                case 'job_run_ended': {
                    _this.props.updateRunState(eventCopy, _this.context.config);
                    _this.props.updateBranchState(eventCopy, _this.context.config);
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

OrganizationPipelines.contextTypes = {
    config: object.isRequired,
    params: object.isRequired,
};

OrganizationPipelines.propTypes = {
    fetchPipelines: func.isRequired,
    fetchPipelinesIfNeeded: func.isRequired,
    setPipeline: func,
    processJobQueuedEvent: func.isRequired,
    updateRunState: func.isRequired,
    updateBranchState: func.isRequired,
    organization: string,
    params: object, // From react-router
    children: node, // From react-router
    location: object, // From react-router
    pipelines: array,
};

OrganizationPipelines.childContextTypes = {
    pipelines: array,
    pipeline: object,
};

const selectors = createSelector([pipelinesSelector],
    (pipelines) => ({ pipelines }));

export default connect(selectors, actions)(OrganizationPipelines);

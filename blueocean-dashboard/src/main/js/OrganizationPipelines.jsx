import React, { Component, PropTypes } from 'react';
import {
    actions,
    allPipelines as allPipelinesSelector,
    organizationPipelines as organizationPipelinesSelector,
    connect,
    createSelector,
} from './redux';
import loadingIndicator from './LoadingIndicator';
import * as sse from '@jenkins-cd/sse-gateway';
import * as pushEventUtil from './util/push-event-util';

const { object, array, func, node, string } = PropTypes;

class OrganizationPipelines extends Component {
    // FIXME: get rid of context use
    getChildContext() {
        if (this._getOrganizationName()) {
            return {
                pipelines: this.props.organizationPipelines,
            };
        }
        return {
            pipelines: this.props.allPipelines,
        };
    }
    
    componentWillMount() {
        const config = this.context.config;
        if (config) {
            const organizationName = this._getOrganizationName();
            
            if (organizationName) {
                this.props.getOrganizationPipelines({ organizationName });
            } else {
                this.props.getAllPipelines();
            }
            
            // Subscribe for job channel push events
            this.jobListener = sse.subscribe('job', (event) => {
                // Enrich the event with blueocean specific properties
                // before passing it on to be processed.
                const eventCopy = pushEventUtil.enrichJobEvent(event, this.props.params.pipeline);

                // See http://jenkinsci.github.io/pubsub-light-module/org/jenkins/pubsub/Events.JobChannel.html
                switch (eventCopy.jenkins_event) {
                case 'job_crud_created':
                case 'job_crud_deleted':
                case 'job_crud_renamed':
                    // Just refetch and update the pipelines and branches list.
                    // Yes, in some of these cases it would be possible to
                    // update the redux store state without making a REST call.
                    // Trading off for simplicity and view consistency here.
                    // Doing it this way leaves the code a lot simpler + guarantees
                    // That the user sees the pipelines in the same order etc as they
                    // would if they did a page reload. Also remember that these
                    // crud operations are relative low frequency, so not much
                    // benefit to be got from optimizing things here.
                    // TODO: fix https://issues.jenkins-ci.org/browse/JENKINS-35153 for delete
                    if (this._getOrganizationName()) {
                        this.props.fetchOrganizationPipelines({ organizationName: this._getOrganizationName() });
                    } else {
                        this.props.fetchAllPipelines();
                    }
                    this.props.updateBranchList(eventCopy, this.context.config);
                    break;
                case 'job_run_queue_buildable':
                case 'job_run_queue_enter':
                    this.props.processJobQueuedEvent(eventCopy);
                    break;
                case 'job_run_queue_left':
                    this.props.processJobLeftQueueEvent(eventCopy);
                    break;
                case 'job_run_queue_blocked': {
                    break;
                }
                case 'job_run_started': {
                    this.props.updateRunState(eventCopy, this.context.config, true);
                    this.props.updateBranchState(eventCopy, this.context.config);
                    break;
                }
                case 'job_run_ended': {
                    this.props.updateRunState(eventCopy, this.context.config);
                    this.props.updateBranchState(eventCopy, this.context.config);
                    break;
                }
                default :
                // Else ignore the event.
                }
            });
        }
    }

    componentDidMount() {
        loadingIndicator.setDarkBackground();
    }
    
    componentWillReceiveProps(nextProps) {
        const organizationName = this._getOrganizationName(nextProps);
        if (this._getOrganizationName(this.props) !== organizationName) {
            if (organizationName) {
                this.props.getOrganizationPipelines({ organizationName });
            } else {
                this.props.getAllPipelines();
            }
        }
    }

    componentWillUnmount() {
        if (this.jobListener) {
            sse.unsubscribe(this.jobListener);
            delete this.jobListener;
        }
        loadingIndicator.setLightBackground();
    }
    
    _getOrganizationName(nextProps) {
        if (nextProps && nextProps.params) {
            return nextProps.params.organization;
        }
        if (this.props && this.props.params && this.props.params.organization) {
            return this.props.params.organization;
        }
        if (this.context && this.context.params && this.context.params.organization) {
            return this.context.params.organization;
        }
        return null;
    }

    /*
     FIXME we should use clone here, this way we could pass all actions and reducer down to all
     components and get rid of the seperate connect in each subcomponents -> see RunDetailsPipeline
     */
    render() {
        if (!this.props.allPipelines && !this.props.organizationPipelines) {
            return null;
        }
        return this.props.children;
    }
}

OrganizationPipelines.contextTypes = {
    config: object.isRequired,
    params: object.isRequired,
};

OrganizationPipelines.propTypes = {
    fetchAllPipelines: func.isRequired,
    fetchOrganizationPipelines: func.isRequired,
    getAllPipelines: func.isRequired,
    getOrganizationPipelines: func.isRequired,
    processJobQueuedEvent: func.isRequired,
    processJobLeftQueueEvent: func.isRequired,
    updateRunState: func.isRequired,
    updateBranchState: func.isRequired,
    updateBranchList: func.isRequired,
    organization: string,
    params: object, // From react-router
    children: node, // From react-router
    location: object, // From react-router
    allPipelines: array,
    organizationPipelines: array,
};

OrganizationPipelines.childContextTypes = {
    pipelines: array,
};

const selectors = createSelector([allPipelinesSelector, organizationPipelinesSelector],
    (allPipelines, organizationPipelines) => ({ allPipelines, organizationPipelines }));

export default connect(selectors, actions)(OrganizationPipelines);

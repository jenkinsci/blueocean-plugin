/**
 * Simple widget for triggering the running of a pipeline.
 */

import React, { Component, PropTypes } from 'react';
import Pipeline from '../api/Pipeline';
import Branch from '../api/Branch';
import { ToastService as toastService } from '@jenkins-cd/blueocean-core-js';
import assert from 'assert';

export default class RunPipeline extends Component {

    constructor(props) {
        super(props);
        assert(props.organization, 'RunPipeline: "organization" not defined');
        assert(props.pipeline, 'RunPipeline: "pipeline" not defined');

        if (props.branch) {
            this.pipeline = new Branch(props.organization, props.pipeline, props.branch);
        } else {
            this.pipeline = new Pipeline(props.organization, props.pipeline);
        }

        this.buttonText = (props.buttonText ? props.buttonText : '');
        this.buttonClass = (props.buttonClass ? props.buttonClass : '');
    }

    componentDidMount() {
        const reactContext = this.context;
        const thePipeline = this.pipeline;

        this.pipeline.onJobChannelEvent((event) => {
            if (event.jenkins_event === 'job_run_started') {
                toastService.newToast({
                    text: `Started "${thePipeline.branchName}" #${event.jenkins_object_id}`,
                    action: 'Open',
                    onActionClick: () => {
                        const runDetailsUrl = thePipeline.runDetailsRouteUrl(event.jenkins_object_id);
                        reactContext.location.pathname = runDetailsUrl;
                        reactContext.router.push(runDetailsUrl);
                    },
                });
            }
        });
    }

    componentWillUnmount() {
        this.pipeline.clearEventListeners();
    }

    run(event) {
        const thePipeline = this.pipeline;

        this.pipeline.run(() => {
            toastService.newToast({
                text: `Queued "${thePipeline.branchName}"`,
            });
        }, (error) => {
            console.error(`Unexpected error queuing a run of "${thePipeline.branchName}". Response:`);
            console.error(error);
            toastService.newToast({
                text: `Failed to queue "${thePipeline.branchName}". Try reloading the page.`,
            });
        });

        event.stopPropagation();
    }

    render() {
        const _this = this;
        return (
            <div className={`run-pipeline ${_this.buttonClass}`} onClick={(event) => this.run(event)}>{_this.buttonText}</div>
        );
    }
}

RunPipeline.propTypes = {
    organization: PropTypes.string,
    pipeline: PropTypes.string,
    branch: PropTypes.string,
    buttonClass: PropTypes.string,
    buttonText: PropTypes.string,
};

RunPipeline.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object,
};

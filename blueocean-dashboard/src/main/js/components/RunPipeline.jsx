/**
 * Simple widget for triggering the running of a pipeline.
 */

import React, { Component, PropTypes } from 'react';
import Pipeline from '../api/Pipeline';
import Branch from '../api/Branch';
import { Toast } from '@jenkins-cd/design-language';
import * as sse from '@jenkins-cd/sse-gateway';
import * as pushEventUtil from '../util/push-event-util';

export default class RunPipeline extends Component {

    constructor(props) {
        super(props);
        const pipeline = new Pipeline(props.organization, props.pipeline);
        this.branch = new Branch(pipeline, props.branch);
        this.state = {
            isShowToast: false
        };
    }

    componentDidMount() {
        const _this = this;
        this.jobListener = sse.subscribe('job', (event) => {
            // Enrich the event with blueocean specific properties.
            // This allows us to make sense of a regular Jenkins event
            // in the context of how Blue Ocean conceptualizes them.
            const eventCopy = pushEventUtil.enrichJobEvent(event);
            if (eventCopy.blueocean_is_multi_branch &&
                eventCopy.blueocean_job_name === _this.branch.pipeline.name &&
                eventCopy.blueocean_branch_name === _this.branch.name) {
                if (event.jenkins_event === 'job_run_started') {
                    _this.setState({ isShowToast: true });
                } else {
                    _this.setState({ isShowToast: false });
                }
            }
        });
    }

    componentWillUnmount() {
        if (this.jobListener) {
            sse.unsubscribe(this.jobListener);
            delete this.jobListener;
        }
    }

    run() {
        this.branch.run();
    }

    render() {
        if (this.state.isShowToast) {
            return (<div>
                <div className="run-pipeline" onClick={() => this.run()}></div>
                <div className="run-pipeline-toast">
                    <Toast text="Started" action="Open" />
                </div>
            </div>);
        } else {
            return (<div className="run-pipeline" onClick={() => this.run()}></div>);
        }
    }
}

RunPipeline.propTypes = {
    organization: PropTypes.string,
    pipeline: PropTypes.string,
    branch: PropTypes.string,
};

/**
 * Simple widget for triggering the running of a pipeline.
 */

import React, { Component, PropTypes } from 'react';
import Pipeline from '../api/Pipeline';
import Branch, { fromSSEEvent } from '../api/Branch';
import { Toast } from '@jenkins-cd/design-language';
import * as sse from '@jenkins-cd/sse-gateway';

export default class RunPipeline extends Component {

    constructor(props) {
        super(props);
        const pipeline = new Pipeline(props.organization, props.pipeline);
        this.branch = new Branch(pipeline, props.branch);
        this.state = {
            toast: undefined,
        };
    }

    componentDidMount() {
        const _this = this;
        const reactContext = this.context;
        this.jobListener = sse.subscribe('job', (event) => {
            const eventBranch = fromSSEEvent(event);
            if (_this.branch.equals(eventBranch)) {
                if (event.jenkins_event === 'job_run_queue_enter') {
                    _this.setState({
                        toast: { text: 'ueued' },
                    });
                } else if (event.jenkins_event === 'job_run_started') {
                    _this.setState({
                        toast: {
                            text: `Started "${eventBranch.name}" #${event.jenkins_object_id}`,
                            action: {
                                label: 'Open',
                                callback: () => {
                                    const runDetailsUrl = eventBranch.runDetailsRouteUrl(event.jenkins_object_id);
                                    reactContext.location.pathname = runDetailsUrl;
                                    reactContext.router.push(runDetailsUrl);
                                },
                            },
                        },
                    });
                } else {
                    _this.setState({ toast: undefined });
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
        const toast = this.state.toast;
        if (toast) {
            if (toast.action) {
                return (<div>
                    <div className="run-pipeline" onClick={() => this.run()}></div>
                    <div className="run-pipeline-toast">
                        <Toast text={toast.text} action={toast.action.label} onActionClick={() => toast.action.callback()} />
                    </div>
                </div>);
            }
            return (<div>
                <div className="run-pipeline" onClick={() => this.run()}></div>
                <div className="run-pipeline-toast">
                    <Toast text={toast.text} />
                </div>
            </div>);
        }
        return (<div className="run-pipeline" onClick={() => this.run()}></div>);
    }
}

RunPipeline.propTypes = {
    organization: PropTypes.string,
    pipeline: PropTypes.string,
    branch: PropTypes.string,
};

RunPipeline.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object,
};

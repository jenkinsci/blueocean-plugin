/**
 * Simple widget for triggering the running of a pipeline.
 */

import React, { Component, PropTypes } from 'react';
import Pipeline from '../api/Pipeline';
import Branch from '../api/Branch';
import { Toast } from '@jenkins-cd/design-language';
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

        this.state = {
            toast: undefined,
        };

        this.buttonText = (props.buttonText ? props.buttonText : '');
        this.buttonClass = (props.buttonClass ? props.buttonClass : '');
    }

    componentDidMount() {
        const _this = this;
        const reactContext = this.context;
        const thePipeline = this.pipeline;

        this.pipeline.onJobChannelEvent((event) => {
            if (event.jenkins_event === 'job_run_started') {
                _this.setState({
                    toast: {
                        text: `Started "${thePipeline.branchName}" #${event.jenkins_object_id}`,
                        action: {
                            label: 'Open',
                            callback: () => {
                                const runDetailsUrl = thePipeline.runDetailsRouteUrl(event.jenkins_object_id);
                                reactContext.location.pathname = runDetailsUrl;
                                reactContext.router.push(runDetailsUrl);
                            },
                        },
                    },
                });
            } else {
                _this.setState({ toast: undefined });
            }
        });
    }

    componentWillUnmount() {
        this.pipeline.clearEventListeners();
    }

    run(event) {
        const _this = this;
        const thePipeline = this.pipeline;

        this.pipeline.run(() => {
            // Success...
            _this.setState({
                toast: { text: `Queued "${thePipeline.branchName}"` },
            });
        }, (error) => {
            // Fail...
            console.error(`Unexpected error queuing a run of "${thePipeline.branchName}". Response:`);
            console.error(error);
            _this.setState({
                toast: { text: `Failed to queue "${thePipeline.branchName}". Try reloading the page.` },
            });
        });
        
        event.stopPropagation();
    }

    render() {
        const _this = this;
        const toast = this.state.toast;
        if (toast) {
            if (toast.action) {
                return (<div>
                    <div className={`run-pipeline ${_this.buttonClass}`} onClick={(event) => this.run(event)}>{_this.buttonText}</div>
                    <div className="run-pipeline-toast">
                        <Toast text={toast.text} action={toast.action.label} onActionClick={() => toast.action.callback()} />
                    </div>
                </div>);
            }
            return (<div>
                <div className={`run-pipeline ${_this.buttonClass}`} onClick={(event) => this.run(event)}>{_this.buttonText}</div>
                <div className="run-pipeline-toast">
                    <Toast text={toast.text} />
                </div>
            </div>);
        }
        return (<div className={`run-pipeline ${_this.buttonClass}`} onClick={(event) => this.run(event)}>{_this.buttonText}</div>);
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

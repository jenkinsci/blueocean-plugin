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
            this.branch = new Branch(props.organization, props.pipeline, props.branch);
        } else {
            this.branch = new Pipeline(props.organization, props.pipeline);
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
        const theBranch = this.branch;

        this.branch.onJobChannelEvent((event) => {
            if (event.jenkins_event === 'job_run_started') {
                _this.setState({
                    toast: {
                        text: `Started "${theBranch.branchName}" #${event.jenkins_object_id}`,
                        action: {
                            label: 'Open',
                            callback: () => {
                                const runDetailsUrl = theBranch.runDetailsRouteUrl(event.jenkins_object_id);
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
        this.branch.clearEventListeners();
    }

    run(event) {
        const _this = this;
        const theBranch = this.branch;

        this.branch.run(() => {
            // Success...
            _this.setState({
                toast: { text: `Queued "${theBranch.branchName}"` },
            });
        }, (error) => {
            // Fail...
            console.error(`Unexpected error queuing a run of "${theBranch.branchName}". Response:`);
            console.error(error);
            _this.setState({
                toast: { text: `Failed to queue "${theBranch.branchName}". Try reloading the page.` },
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

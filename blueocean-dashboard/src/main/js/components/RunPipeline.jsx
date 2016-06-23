/**
 * Simple widget for triggering the running of a pipeline.
 */

import React, { Component, PropTypes } from 'react';
import Pipeline from '../api/Pipeline';
import Branch from '../api/Branch';
import { Toast } from '@jenkins-cd/design-language';

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
        const theBranch = this.branch;

        this.branch.onJobChannelEvent((event) => {
            if (event.jenkins_event === 'job_run_queue_enter') {
                _this.setState({
                    toast: { text: `Queued "${theBranch.name}"` },
                });
            } else if (event.jenkins_event === 'job_run_started') {
                _this.setState({
                    toast: {
                        text: `Started "${theBranch.name}" #${event.jenkins_object_id}`,
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

        this.branch.run((response) => {
            console.error(`Unexpected error queuing a run of "${theBranch.name}". Response:`);
            console.error(response);
            _this.setState({
                toast: { text: `Failed to queue "${theBranch.name}". Try reloading the page.` },
            });
        });
        
        event.stopPropagation();
    }

    render() {
        const toast = this.state.toast;
        if (toast) {
            if (toast.action) {
                return (<div>
                    <div className="run-pipeline" onClick={(event) => this.run(event)}></div>
                    <div className="run-pipeline-toast">
                        <Toast text={toast.text} action={toast.action.label} onActionClick={() => toast.action.callback()} />
                    </div>
                </div>);
            }
            return (<div>
                <div className="run-pipeline" onClick={(event) => this.run(event)}></div>
                <div className="run-pipeline-toast">
                    <Toast text={toast.text} />
                </div>
            </div>);
        }
        return (<div className="run-pipeline" onClick={(event) => this.run(event)}></div>);
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

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
            isShowToast: false
        };
    }

    run() {
        this.setState({ isShowToast: true });
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

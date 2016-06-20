
import React, { Component, PropTypes } from 'react';
import Pipeline from '../api/Pipeline';
import Branch from '../api/Branch';

export default class RunPipeline extends Component {

    constructor(props) {
        super(props);
        const pipeline = new Pipeline(props.organization, props.pipeline);
        this.branch = new Branch(pipeline, props.branch);
    }

    run() {
        this.branch.run();
    }

    render() {
        return (<div className="run-pipeline" onClick={() => this.run()}></div>);
    }
}

RunPipeline.propTypes = {
    organization: PropTypes.string,
    pipeline: PropTypes.string,
    branch: PropTypes.string,
};

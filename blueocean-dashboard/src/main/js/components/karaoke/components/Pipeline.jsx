import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pipeline');

export default class Pipeline extends Component {
    constructor(props) {
        super(props);
        logger.warn('props constructor', props);
    }
    render() {
        logger.warn('props', this.props);
        return (<div ref="scrollArea">
            Invoke PipelineComponent now  pager: {this.props.augmenter.isPipeline}
        </div>);
    }
}

Pipeline.propTypes = {
    pipeline: PropTypes.object,
    pager: PropTypes.object,
    branch: PropTypes.string,
    runId: PropTypes.string,
};

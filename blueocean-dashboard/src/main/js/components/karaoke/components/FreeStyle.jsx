import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.FreeStyle');

export class FreeStyle extends Component {
    constructor(props) {
        super(props);
        logger.warn('props', props);
    }
    render() {
        logger.warn('props', this.props);
        return (<div ref="scrollArea">
            Invoke FreeStyleComponent now
        </div>);
    }
}

FreeStyle.propTypes = {
    pipeline: PropTypes.object,
    pager: PropTypes.object,
    branch: PropTypes.string,
    runId: PropTypes.string,
};

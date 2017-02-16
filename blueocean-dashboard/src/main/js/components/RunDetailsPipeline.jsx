import React, { Component, PropTypes } from 'react';
import {
    logging,
} from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';

import { KaraokeService } from './karaoke/index';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.RunDetailsPipeline');

@observer
export class RunDetailsPipeline extends Component {

    componentWillMount() {
        if (this.props.params) {
            this.generateUrl(this.props);
        }
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.params) {
            this.generateUrl(nextProps);
        }
    }

    generateUrl(props) {
        const { pipeline, params: { branch, runId } } = props;
        logger.warn('debugger')
        this.pager = KaraokeService.karaokePager(pipeline, branch, runId);
    }

    render() {
        logger.warn('this.props', this.props);
        if(this.pager.pending) {
            logger.debug('abort due to pager pending');
            return null;
        }
        const run = this.pager.data;
        // logger.warn('this.pager.data', this.pager.data);
        if(this.pager.isFreeStyle){
            return (
                <div ref="scrollArea">
                    Invoke FreeStyleComponent now
                </div>
            );
        }
        if(this.pager.isPipeline) {
            return (
                <div ref="scrollArea">
                    Pipe it baby
                </div>
            );

        }
        return (
                <div ref="scrollArea">
                    DOH type not supported
                </div>
            )
    }

}

RunDetailsPipeline.propTypes = {
    pipeline: PropTypes.object,
    isMultiBranch: PropTypes.any,
    params: PropTypes.object,
    result: PropTypes.object,
    fileName: PropTypes.string,
    url: PropTypes.string,
    fetchNodes: PropTypes.func,
    setNode: PropTypes.func,
    fetchSteps: PropTypes.func,
    removeStep: PropTypes.func,
    removeLogs: PropTypes.func,
    cleanNodePointer: PropTypes.func,
    steps: PropTypes.object,
    nodes: PropTypes.object,
    nodeReducer: PropTypes.object,
    t: PropTypes.func,
};

RunDetailsPipeline.contextTypes = {
    config: PropTypes.object.isRequired,
    params: PropTypes.object,
    pipeline: PropTypes.object,
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object.isRequired, // From react-router
};


export default RunDetailsPipeline;

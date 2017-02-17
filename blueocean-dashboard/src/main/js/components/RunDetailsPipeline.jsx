import React, { Component, PropTypes } from 'react';
import { logging, } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import Extensions from '@jenkins-cd/js-extensions';
import { FreeStyle} from './karaoke/components/FreeStyle';
import { Pipeline } from './karaoke/components/Pipeline';

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
        const { pipeline, params: { branch, runId } } = this.props;
        const run = this.pager.data;
        // logger.warn('this.pager.data', this.pager.data);
        if(this.pager.isFreeStyle){
            return (<Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.freestyle.provider',
                        pager: this.pager,
                        pipeline,
                        branch,
                        runId,
                    }
                } />);
        }
        if(this.pager.isPipeline) {
            return (<Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.pipeline.provider',
                        pager: this.pager,
                        pipeline,
                        branch,
                        runId,
                    }
            } />);

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

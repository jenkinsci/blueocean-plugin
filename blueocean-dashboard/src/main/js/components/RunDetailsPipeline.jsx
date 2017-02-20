import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import Extensions from '@jenkins-cd/js-extensions';
import { KaraokeService } from './karaoke/index';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.RunDetailsPipeline');

@observer
export class RunDetailsPipeline extends Component {

    componentWillMount() {
        if (this.props.params) {
            this.fetchData(this.props);
        }
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.params) {
            this.fetchData(nextProps);
        }
    }

    fetchData(props) {
        const { pipeline, params: { branch, runId } } = props;
        this.pager = KaraokeService.karaokePager(pipeline, branch, runId);
    }

    render() {
        if (this.pager.pending) {
            logger.debug('abort due to pager pending');
            return null;
        }
        const { pipeline, params: { branch, runId }, t } = this.props;
        const { router, location } = this.context;
        logger.warn('xxx', this.props);
        const commonProps = {
            pager: this.pager,
            pipeline,
            branch,
            runId,
            t,
            router,
            location,
        };
        if (this.pager.isFreeStyle) {
            return (<Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.freestyle.provider',
                        ...commonProps,
                    }
                }
            />);
        }
        if (this.pager.isPipeline) {
            return (<Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.pipeline.provider',
                        ...commonProps,
                    }
                }
            />);
        }
        return (
                <div ref="scrollArea">
                    DOH type not supported
                </div>
            );
    }
}

RunDetailsPipeline.propTypes = {
    pipeline: PropTypes.object,
    params: PropTypes.object,
    t: PropTypes.func,
};

RunDetailsPipeline.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object.isRequired, // From react-router
};

export default RunDetailsPipeline;

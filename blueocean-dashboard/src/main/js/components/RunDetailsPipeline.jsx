import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import Extensions from '@jenkins-cd/js-extensions';
import { Augmenter } from './karaoke/services/Augmenter';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.RunDetailsPipeline');

@observer
export class RunDetailsPipeline extends Component {

    constructor(props) {
        super(props);
        // we do not want to follow any builds that are finished
        this.state = { followAlong: props && props.result && props.result.state !== 'FINISHED' || false };
    }

    componentWillMount() {
        if (this.props.params) {
            this.augment(this.props);
        }
    }

    augment(props) {
        const { result: run, pipeline, params: { branch } } = props;
        this.augmenter = new Augmenter(pipeline, branch, run);
    }

    render() {
        const { result: run, pipeline, params: { branch }, t } = this.props;
        const { router, location } = this.context;
        const commonProps = {
            scrollToBottom: this.state.followAlong || (run && run.result === 'FAILURE'),
            augmenter: this.augmenter,
            followAlong: this.state.followAlong,
            t,
            run,
            pipeline,
            branch,
            router,
            location,
        };
        logger.warn('xxx', this.props, commonProps);
        if (this.augmenter.isFreeStyle) {
            return (<Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.freestyle.provider',
                        ...commonProps,
                    }
                }
            />);
        }
        if (this.augmenter.isPipeline) {
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
    result: PropTypes.object,
    params: PropTypes.object,
    t: PropTypes.func,
};

RunDetailsPipeline.contextTypes = {
    router: PropTypes.object.isRequired, // From react-router
    location: PropTypes.object.isRequired, // From react-router
};

export default RunDetailsPipeline;

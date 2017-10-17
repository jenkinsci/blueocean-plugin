import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import Extensions from '@jenkins-cd/js-extensions';
import { PipelineView } from './karaoke/PipelineView';

import { KaraokeConfig } from './karaoke';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.RunDetailsPipeline');
@observer
export class RunDetailsPipeline extends Component {
    constructor(props) {
        super(props);
        this._onScrollHandler = this._onScrollHandler.bind(this);
    }
    componentWillMount() {
        if (this.props.params) {
            logger.debug('Augmenting this.properties');
            this.augment(this.props);
        }
    }
    componentDidMount() {
        const { result } = this.props;
        if (!result.isCompleted()) {
            document.addEventListener('scroll', this._onScrollHandler, true);
            const thisEl = ReactDOM.findDOMNode(this);
            const fullscreenContents = thisEl.parentElement.parentElement; // this is the actual div that scrolls
            this._scrollElement = fullscreenContents;
            // need to focus on something within the popup to handle keyboard scroll input
            fullscreenContents.setAttribute('tabindex', '-1'); // -1 so can't actually tab to it
            fullscreenContents.focus();
        }
    }
    componentWillReceiveProps(nextProps) {
        if (KaraokeConfig.getPreference('runDetails.pipeline.updateOnFinish').value !== 'never') {
            logger.debug('Augmenting next properties');
            this.augment(nextProps);
        } else if (((nextProps.params.runId !== this.props.params.runId) || nextProps.result.id !== this.props.result.id)
            && KaraokeConfig.getPreference('runDetails.pipeline.updateOnFinish').value === 'never') {
            logger.debug('Augmenting next properties - new run needs update');
            this.augment(nextProps);
        } else {
            logger.debug('EarlyOut - dropping nextProps on the floor.');
        }
    }
    componentWillUnmount() {
        document.removeEventListener('scroll', this._onScrollHandler);
    }
    _onScrollHandler() {
        if (this.props.result.isCompleted()) {
            return;
        }
        const el = this._scrollElement;
        const isAtBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 1;
        if (isAtBottom && !this.pipelineView.following) {
            console.log('resuming following, pipelineView is', this.pipelineView.following ? 'active' : 'disabled');
            this.pipelineView.setFollowing(true);
        } else if (!isAtBottom && this.pipelineView.following) {
            console.log('stopping following, pipelineView is', this.pipelineView.following ? 'active' : 'disabled');
            this.pipelineView.setFollowing(false);
        }
    }
    augment(props) {
        // we do not want to follow any builds that are finished
        const { result: run, pipeline, params: { branch } } = props;
        const followAlong = props && props.result && props.result.state !== 'FINISHED' || false;
        if (!this.pipelineView) {
            this.pipelineView = new PipelineView(pipeline, branch, run, followAlong);
            this.pipelineView.setLogActive(followAlong); // default to active, as the component will focus on the first active element
        }
    }
    render() {
        const { result: run, pipeline, params: { branch }, t } = this.props;
        const { router, location } = this.context;
        const commonProps = {
            pipelineView: this.pipelineView,
            t,
            run,
            pipeline,
            branch,
            router,
            location,
            params: this.props.params,
        };
        let provider;
        if (this.pipelineView.isFreeStyle) {
            provider = (<Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.freestyle.provider',
                        ...commonProps,
                    }
                }
            />);
        }
        if (!this.classicLog && this.pipelineView.isPipeline) {
            provider = (<Extensions.Renderer {
                    ...{
                        extensionPoint: 'jenkins.pipeline.karaoke.pipeline.provider',
                        ...commonProps,
                    }
                }
            />);
        }
        const stepScrollAreaClass = `step-scroll-area ${this.pipelineView.following ? 'follow-along-on' : 'follow-along-off'}`;
        return (<div className={stepScrollAreaClass} >
            { provider }
        </div>);
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

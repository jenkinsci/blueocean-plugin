import React, { Component, PropTypes } from 'react';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { observer } from 'mobx-react';
import Extensions from '@jenkins-cd/js-extensions';
import { Augmenter } from './karaoke/services/Augmenter';

import { KaraokeConfig } from './karaoke';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.RunDetailsPipeline');
@observer
export class RunDetailsPipeline extends Component {
    constructor(props) {
        super(props);
        this._handleKeys = this._handleKeys.bind(this);
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
        if (!result.isQueued()) {
            document.addEventListener('wheel', this._onScrollHandler, false);
            document.addEventListener('keydown', this._handleKeys, false);
        }
    }
    componentWillReceiveProps(nextProps) {
        if (KaraokeConfig.getPreference('runDetails.pipeline.updateOnFinish').value !== 'never') {
            logger.debug('Augmenting next properties');
            this.augment(nextProps);
        } else if (
            (nextProps.params.runId !== this.props.params.runId || nextProps.result.id !== this.props.result.id) &&
            KaraokeConfig.getPreference('runDetails.pipeline.updateOnFinish').value === 'never'
        ) {
            logger.debug('Augmenting next properties - new run needs update');
            this.augment(nextProps);
        } else {
            logger.debug('EarlyOut - dropping nextProps on the floor.');
        }
    }
    componentWillUnmount() {
        document.removeEventListener('keydown', this._handleKeys);
        document.removeEventListener('wheel', this._onScrollHandler);
    }
    // we bail out on arrow_up, home or page up key and continue on end key
    _handleKeys(event) {
        if ((event.keyCode === 38 || event.keyCode === 36 || event.keyCode === 33) && this.augmenter.karaoke && this.props.result.state !== 'PAUSED') {
            logger.debug('stop follow along by key up, home or page up');
            this.augmenter.setKaraoke(false);
        }
        if ((event.keyCode === 35) && !this.augmenter.karaoke && this.props.result.state !== 'PAUSED') {
            logger.debug('start follow along by key end');
            this.augmenter.setKaraoke(true);
        }
    }
    // need to register handler to step out of karaoke mode
    // we bail out on scroll up
    _onScrollHandler(elem) {
        if (elem.deltaY < 0 && this.augmenter.karaoke && this.props.result.state !== 'PAUSED') {
            logger.debug('stop follow along by scroll up');
            this.augmenter.setKaraoke(false);
        }
    }
    augment(props) {
        // we do not want to follow any builds that are finished
        const {
            result: run,
            pipeline,
            params: { branch },
        } = props;
        const followAlong = (props && props.result && props.result.state !== 'FINISHED') || false;
        this.augmenter = new Augmenter(pipeline, branch, run, followAlong);
    }
    render() {
        const { pipeline, t } = this.props;
        const { router, location, activityService } = this.context;
        const run = this.props.result;
        const branch = this.props.params.branch;

        const commonProps = {
            scrollToBottom: this.augmenter.karaoke,
            augmenter: this.augmenter,
            t,
            run,
            pipeline,
            branch,
            router,
            location,
            activityService,
            params: this.props.params,
        };
        let provider;
        const stepScrollAreaClass = `step-scroll-area ${this.augmenter.karaoke ? 'follow-along-on' : 'follow-along-off'}`;
        if (this.augmenter.isFreeStyle) {
            provider = <Extensions.Renderer extensionPoint="jenkins.pipeline.karaoke.freestyle.provider" {...commonProps} />;
        } else if (!this.classicLog && this.augmenter.isPipeline) {
            provider = <Extensions.Renderer extensionPoint="jenkins.pipeline.karaoke.pipeline.provider" {...commonProps} />;
        }

        return <div className={stepScrollAreaClass}>{provider}</div>;
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
    activityService: PropTypes.object.isRequired,
};

export default RunDetailsPipeline;

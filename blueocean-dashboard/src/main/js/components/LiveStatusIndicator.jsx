import React, { Component, PropTypes } from 'react';
import { LiveStatusIndicator as LiveStatusIndicatorJdl } from '@jenkins-cd/design-language';
import { i18nTranslator, logging } from '@jenkins-cd/blueocean-core-js';
import { TimeManager } from '../util/serverBrowserTimeHarmonize';
/**
 * Translate function
 */
const translate = i18nTranslator('blueocean-dashboard');
const timeManager = new TimeManager();
const logger = logging.logger('io.jenkins.blueocean.dashboard.LiveStatusIndicator');

export class LiveStatusIndicator extends Component {
    componentWillMount() {
        this.durationMillis = this.getDuration();
    }

    getDuration() {
        return this.getTimes().duration;
    }

    getTimes() {
        const { result, startTime, duration } = this.props;
        // we need to make sure that we calculate with the correct time offset
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        return timeManager.harmonizeTimes({
            startTime,
            durationInMillis: duration,
            isRunning: this.isRunningFunction(result)(),
        }, skewMillis);
    }

    isRunningFunction(result) {
        const isRunning = () => {
            switch (result) {
            case 'RUNNING':
            case 'PAUSED':
            case 'QUEUED':
                return true;
            default:
                return false;
            }
        };
        return isRunning;
    }

    render() {
        const { result } = this.props;
        const durationMillis = this.isRunningFunction(result)() ? this.durationMillis : this.getDuration();
        const i18nDuration = timeManager.format(durationMillis, translate('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' }));

        logger.warn('times', { thisDuration: this.durationMillis, i18nDuration, durationMillis });
        const title = translate(`common.state.${result.toLowerCase()}`, { 0: i18nDuration });

        return (
            <div title={title}>
                <LiveStatusIndicatorJdl { ... this.props } />
            </div>
        );
    }
}

LiveStatusIndicator.propTypes = {
    result: PropTypes.string,
    percentage: PropTypes.number,
    width: PropTypes.string,
    height: PropTypes.string,
    noBackground: PropTypes.bool,
    startTime: PropTypes.string,
    estimatedDuration: PropTypes.number,
    duration: PropTypes.number,
};

LiveStatusIndicator.contextTypes = {
    config: PropTypes.object.isRequired,
};

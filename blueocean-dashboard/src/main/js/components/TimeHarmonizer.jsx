import React, { Component, PropTypes } from 'react';
import { i18nTranslator, logging } from '@jenkins-cd/blueocean-core-js';
import { TimeManager } from '../util/serverBrowserTimeHarmonize';

const translate = i18nTranslator('blueocean-dashboard');
const timeManager = new TimeManager();
const logger = logging.logger('io.jenkins.blueocean.dashboard.TimeHarmonizer');

export const TimeHarmonizer = ComposedComponent => {
    class NewComponent extends Component {
        constructor(props, context) {
            super(props, context);
            logger.warn(props, context);
            this.skewMillis = this.context && this.context.config ? this.context.config.getServerBrowserTimeSkewMillis() : 0;
            this.getI18nTitle = this.getI18nTitle.bind(this);
            this.getDuration = this.getDuration.bind(this);
            this.getTimes = this.getTimes.bind(this);
        }

        componentWillMount() {
            this.durationMillis = this.getDuration();
        }

        getDuration() {
            return this.getTimes().durationMillis;
        }

        getTimes() {
            const { result, startTime, duration } = this.props;
            // we need to make sure that we calculate with the correct time offset
            const harmonizeTimes = timeManager.harmonizeTimes({
                startTime,
                durationInMillis: duration,
                isRunning: this.isRunningFunction(result)(),
            }, this.skewMillis);
            logger.warn('Returning object', harmonizeTimes);
            return harmonizeTimes;
        }

        getI18nTitle(result) {
            const durationMillis = this.isRunningFunction(result)() ? this.durationMillis : this.getDuration();
            const i18nDuration = timeManager
              .format(durationMillis,
                translate('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' }));

            logger.warn('times', { thisDuration: this.durationMillis, i18nDuration, durationMillis });
            const title = translate(`common.state.${result.toLowerCase()}`, { 0: i18nDuration });
            return title;
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
            // create a composedComponent and inject the functions we want to expose
            return (<ComposedComponent
              {...this.props}
              {...this.state}
              getTimes={this.getTimes}
              getDuration={this.getDuration}
              getI18nTitle={this.getI18nTitle}
              isRunning={this.isRunning}
            />);
        }
    }

    NewComponent.propTypes = {
        result: PropTypes.string,
        startTime: PropTypes.string,
        duration: PropTypes.number,
    };

    NewComponent.contextTypes = {
        config: PropTypes.object.isRequired,
    };
    return NewComponent;
};


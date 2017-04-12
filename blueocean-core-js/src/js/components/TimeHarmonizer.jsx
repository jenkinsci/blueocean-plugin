/* eslint-disable */ //TODO: RM
import React, { Component, PropTypes } from 'react';
import { TimeManager } from '../utils/TimeManager';
import logging from '../logging';
import i18nTranslator from '../i18n/i18n';

// TODO: De-const this shit
const translateGLOBAL = i18nTranslator('blueocean-web');
const timeManager = new TimeManager();
const logger = logging.logger('io.jenkins.blueocean.core.TimeHarmonizer');

function translate(...args) {
    console.log('called translate', args);
    try {
        let val = translateGLOBAL(...args);
        console.log('got', val);
        return val;
    } catch (e) {
        console.log('got exception', e);
    }
    return 'NUTS';
}

export class TimeHarmonizerUtil {
    // Construct with the owning component for access to props / context
    constructor(owner) {
        this.owner = owner;

        // TODO: I think we can splat this silly thing
        const {startTime} = owner.props;
        this.durationMillis = startTime ? this.getTimes(owner.props).durationMillis : 0;
    }

    // Current server skew
    getSkewMillis = () => {
        return owner.context && owner.context.config ? owner.context.config.getServerBrowserTimeSkewMillis() : 0;
    };

    getDuration = (result) => {
        const durationMillis = this.isRunningFunction(result)() ? this.durationMillis : this.getTimes().durationMillis;
        return durationMillis;
    };

    getTimes = (props) => {
        props = props || this.owner.props;
        const { result, startTime, durationInMillis, endTime } = props;
        if (!startTime) {
            return {};
        }
        // we need to make sure that we calculate with the correct time offset
        const harmonizeTimes = timeManager.harmonizeTimes({
            startTime,
            endTime,
            durationInMillis,
            isRunning: this.isRunningFunction(result)(),
        }, this.getSkewMillis());
        console.log('!!!! harmonizeTimes result', harmonizeTimes); // TODO: RM
        return harmonizeTimes;
    };

    getI18nTitle = (result) => {
        const durationMillis = this.getDuration(result);
        const i18nDuration = timeManager.format(
            durationMillis,
            translate('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' }));

        const title = translate(`common.state.${result.toLowerCase()}`, { 0: i18nDuration });
        return title;
    };

    isRunningFunction = (result) => {
        // FIXME: Why on earth does this return a function?
        switch (result) {
            case 'RUNNING':
            case 'PAUSED':
            case 'QUEUED':
                return () => true;
            default:
                return () => false;
        }
    };
}

export const TimeHarmonizer = ComposedComponent => {
    class NewComponent extends Component {
        constructor(props, context) {
            super(props, context);
            logger.warn(props, context);
            const { startTime } = this.props;
            this.skewMillis = this.context && this.context.config ? this.context.config.getServerBrowserTimeSkewMillis() : 0;
            this.durationMillis = startTime ? this.getTimes(props).durationMillis : 0;
            this.getI18nTitle = this.getI18nTitle.bind(this);
            this.getDuration = this.getDuration.bind(this);
            this.getTimes = this.getTimes.bind(this);
        }

        getDuration(result) {
            const durationMillis = this.isRunningFunction(result)() ? this.durationMillis : this.getTimes().durationMillis;
            return durationMillis;
        }

        getTimes(props = this.props) {
            const { result, startTime, durationInMillis, endTime } = props;
            if (!startTime) {
                return {};
            }
            // we need to make sure that we calculate with the correct time offset
            const harmonizeTimes = timeManager.harmonizeTimes({
                startTime,
                endTime,
                durationInMillis,
                isRunning: this.isRunningFunction(result)(),
            }, this.skewMillis);
            logger.warn('Returning object', harmonizeTimes);
            return harmonizeTimes;
        }

        getI18nTitle(result) {
            const durationMillis = this.getDuration(result);
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
            const childProps = {
                ...this.props,
                ...this.state,
                getTimes: this.getTimes,
                getDuration: this.getDuration,
                getI18nTitle: this.getI18nTitle,
                isRunning: this.isRunning,
            };

            // create a composedComponent and inject the functions we want to expose
            return (<ComposedComponent {...childProps}/>);
        }
    }

    NewComponent.propTypes = {
        result: PropTypes.string,
        startTime: PropTypes.string,
        endTime: PropTypes.string,
        durationInMillis: PropTypes.number,
    };

    NewComponent.contextTypes = {
        config: PropTypes.object.isRequired,
    };
    return NewComponent;
};


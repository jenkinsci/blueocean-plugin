/* eslint-disable */ //TODO: RM
import React, { Component, PropTypes } from 'react';
import { TimeManager } from '../utils/TimeManager';
import logging from '../logging';
import i18nTranslator from '../i18n/i18n';


function jobStillActive(status) {
    switch (String(status).toUpperCase()) {
        case 'RUNNING':
        case 'PAUSED':
        case 'QUEUED':
            return true;
        default:
            return false;
    }
}

export class TimeHarmonizerUtil {
    // Construct with the owning component for access to props / context
    constructor(owner) {
        this.owner = owner;

        // TODO: I think we can splat this silly thing?
        const {startTime} = owner.props;
        this.durationMillis = startTime ? this.getTimes(owner.props).durationMillis : 0;
    }

    // Current server skew
    getSkewMillis = () => {
        const ctx = this.owner.context;
        return ctx && ctx.config ? ctx.config.getServerBrowserTimeSkewMillis() : 0;
    };

    getDuration = (result) => {
        // TODO: Simplify this?
        const durationMillis = jobStillActive(result) ? this.durationMillis : this.getTimes().durationMillis;
        return durationMillis;
    };

    getTimes = (props) => {
        props = props || this.owner.props;
        const { result, startTime, durationInMillis, endTime } = props;
        if (!startTime) {
            return {};
        }

        // we need to make sure that we calculate with the correct time offset
        return TimeHarmonizerUtil.timeManager.harmonizeTimes({
            startTime,
            endTime,
            durationInMillis,
            isRunning: jobStillActive(result),
        }, this.getSkewMillis());
    };

    getI18nTitle = (result) => {
        const durationMillis = this.getDuration(result);
        const i18nDuration = TimeHarmonizerUtil.timeManager.format(
            durationMillis,
            TimeHarmonizerUtil.translate('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' }));

        const title = TimeHarmonizerUtil.translate(`common.state.${result.toLowerCase()}`, { 0: i18nDuration });
        return title;
    };
}

TimeHarmonizerUtil.timeManager = new TimeManager();
TimeHarmonizerUtil.translate = i18nTranslator('blueocean-web');
TimeHarmonizerUtil.logger = logging.logger('io.jenkins.blueocean.core.TimeHarmonizer');

export const TimeHarmonizer = ComposedComponent => {

    class NewComponent extends Component {

        componentWillMount() {
            this.timeHarmonizerUtil = new TimeHarmonizerUtil(this);
        }

        render() {
            const util = this.timeHarmonizerUtil;
            const { result } = this.props;

            const childProps = {
                ...this.props,
                getTimes: util.getTimes,
                getDuration: util.getDuration,
                getI18nTitle: util.getI18nTitle,
            };

            if (result) {
                childProps.isRunning = jobStillActive(result);
            }

            // create a composedComponent and inject the functions we want to expose
            return (<ComposedComponent {...childProps}/>);
        }
    }

    NewComponent.composedComponent = ComposedComponent;

    NewComponent.propTypes = {
        ...ComposedComponent.propTypes,
        result: PropTypes.string,
        startTime: PropTypes.string,
        endTime: PropTypes.string,
        durationInMillis: PropTypes.number,
    };

    NewComponent.contextTypes = {
        ...ComposedComponent.contextTypes,
        config: PropTypes.object.isRequired,
    };

    return NewComponent;
};



// @flow

import React, { Component, PropTypes } from 'react';
import moment from 'moment';

// So moment is properly initialized:
import 'moment-duration-format';
import 'moment/min/locales.min';

type Props = {
    millis: number,
    updatePeriod: number,
    liveUpdate: boolean,
    displayFormat: ?string,
    liveFormat: ?string,
    locale: ?string,
};

type State = {
    elapsed: number,
};

/**
 * Displays a millisecond duration as text in moment-duration-format's "humanize()" format,
 * e.g. "a few seconds", "2 hours", etc.
 * Set liveUpdate=true to tick the duration up as time elapses.
 *
 * Properties:
 * "millis": number or string.
 * "liveUpdate": boolean
 */
export class TimeDuration extends Component {
    props: Props;
    state: State;
    timerPeriodMillis: number;
    clearIntervalId: number;

    constructor(props: Props) {
        super(props);
        // track how much time has elapsed since live updating tracking started
        this.state = { elapsed: 0 };
        const { updatePeriod = 5000 } = this.props;
        this.timerPeriodMillis = typeof updatePeriod !== 'number' || isNaN(updatePeriod) ? 5000 : updatePeriod;
        this.clearIntervalId = 0;
    }

    componentWillMount() {
        this._handleProps(this.props);
    }

    componentWillReceiveProps(nextProps: Props) {
        this._handleProps(nextProps);
    }

    _handleProps(props: Props) {
        if (this.clearIntervalId) {
            clearInterval(this.clearIntervalId);
            this.clearIntervalId = 0;
        }

        if (props.millis >= 0 && props.liveUpdate) {
            this.clearIntervalId = setInterval(() => {
                this._updateTime();
            }, this.timerPeriodMillis);
        }

        // if live update is disabled, we no longer need to track elapsed time
        if (!props.liveUpdate) {
            this.setState({
                elapsed: 0,
            });
        }
    }

    _updateTime() {
        const elapsed = this.state.elapsed + this.timerPeriodMillis;
        this.setState({
            elapsed,
        });
    }

    static format(value, t, locale) {
        const displayFormat = t('common.date.duration.display.format', { defaultValue: 'd[d] h[h] m[m] s[s]' });

        moment.locale(locale);

        if (!isNaN(value)) {
            if (value < 1000) {
                return '<1s';
            }
            return moment.duration(value).format(displayFormat);
        } else {
            return '-';
        }
    }

    componentWillUnmount() {
        if (this.clearIntervalId) {
            clearInterval(this.clearIntervalId);
            this.clearIntervalId = 0;
        }
    }

    render() {
        const millis = parseInt(this.props.millis) + this.state.elapsed;

        if (!isNaN(millis)) {
            if (millis < 1000) {
                return <span>&#x3C;1s</span>;
            }

            const { locale = 'en', t } = this.props;
            const duration = TimeDuration.format(millis, t, locale);

            return <span>{duration}</span>;
        }

        return <span>-</span>;
    }
}

TimeDuration.propTypes = {
    millis: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    updatePeriod: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    liveUpdate: PropTypes.bool,
    locale: PropTypes.string,
    t: PropTypes.func,
};

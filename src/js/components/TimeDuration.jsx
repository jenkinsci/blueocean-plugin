// @flow

import React, {Component, PropTypes} from 'react';
import moment from 'moment';
require('moment-duration-format');

const { number, oneOfType, string} = PropTypes;

/**
 * Displays a millisecond duration as text in moment-duration-format's "humanize()" format,
 * e.g. "a few seconds", "2 hours", etc.
 * Also displays tooltip with more precise duration in "mos, days, hours, mins, secs" format.
 * Tooltip text can be overridden via "hint" property.
 *
 * Properties:
 * "millis": number or string.
 * "hint": string to use for tooltip.
 */
export class TimeDuration extends Component {
    render() {
        const millis = !isNaN(this.props.millis) ?
            parseInt(this.props.millis) :
            this.props.millis;

        if (!isNaN(millis)) {
            const duration = moment.duration(millis).humanize();

            const hint = this.props.hint ?
                this.props.hint :
                moment.duration(millis).format("M [mos], d [days], h[h], m[m], s[s]");

            return (
                <span title={hint}>{duration}</span>
            );
        }

        return (<span>-</span>);
    }
}

TimeDuration.propTypes = {
    millis: oneOfType([number, string]),
    hint: string,
};
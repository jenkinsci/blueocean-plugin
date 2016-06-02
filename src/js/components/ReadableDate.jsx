// @flow

import React, {Component, PropTypes} from 'react';
import moment from 'moment';

/**
 * Displays a date in moment's "fromNow" format, e.g. "2 hours ago", "5 days ago" etc
 * Also displays the original date on hover.
 * Expects "date" to be passed in as ISO-8601 string with time zone info.
 * If time zone is omitted, then UTC is assumed.
 */
export class ReadableDate extends Component {
    constructor() {
        super();
    }

    render() {
        if (this.props.date) {
            // enforce a ISO-8601 date and try to set proper timezone
            const date = moment(this.props.date, moment.ISO_8601)
                .utcOffset(this.props.date);

            if (date.isValid()) {
                const now = moment().utc();

                // only show the year if from different year
                let tooltip = date.year() !== now.year() ?
                    date.format('MMM DD YYYY h:mma Z') :
                    date.format('MMM DD h:mma Z');

                tooltip = tooltip.replace('+00:00', 'UTC');

                return (
                    <time dateTime={this.props.date} title={tooltip}>{date.fromNow()}</time>
                );
            }
        }

        return (<span>-</span>);
    }
}

ReadableDate.propTypes = {
    date: PropTypes.string,
};

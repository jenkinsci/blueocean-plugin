// @flow

import React, {Component, PropTypes} from 'react';
import moment from 'moment';

/**
 * Displays a date in moment's "fromNow" format, e.g. "2 hours ago", "5 days ago" etc
 * Also displays the original date on hover.
 * Expects "date" to be passed in as ISO-8601 string
 */
export class ReadableDate extends Component {
    constructor() {
        super();
    }

    render() {
        if (this.props.date) {
            const date = moment(this.props.date, moment.ISO_8601);

            if (date.isValid()) {
                return (
                    <span title={this.props.date}>{date.fromNow()}</span>
                );
            }
        }

        return (<span>-</span>);
    }
}

ReadableDate.propTypes = {
    date: PropTypes.string,
};

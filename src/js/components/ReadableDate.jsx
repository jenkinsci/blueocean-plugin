import React, {Component, PropTypes} from 'react';
import moment from 'moment';

/**
 * Displays a date in moment's "fromNow" format, e.g. "2 hours ago", "5 days ago" etc
 * Also displays the original date on hover.
 * Expects "date" to be passed in as ISO string (or any parseable by moment)
 */
export default class ReadableDate extends Component {
    constructor() {
        super();
    }

    render() {
        const display = moment(this.props.date).fromNow();

        return (
            <span title={this.props.date}>{display}</span>
        );
    }
}

ReadableDate.propTypes = {
    date: PropTypes.string.isRequired,
};

// @flow

import React, {Component, PropTypes} from 'react';

const { oneOfType, number, string } = PropTypes;
const units = ['bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];

// Polyfill for old browsers and IE
// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/log10
const log10 = Math.log10 || function (x) {
    return Math.log(x) / Math.LN10;
};

export class FileSize extends Component {
    render() {
        let { bytes } = this.props;
        let output = '-';

        if (typeof bytes == 'string') {
            bytes = parseInt(bytes);
        }

        if (bytes === 0) {
             output = `0 ${units[0]}`;
        } else if (!isNaN(bytes)) {
            // calculate the unit (e.g. 'MB') to display
            // but ensure it doesn't go over the max we support
            let power = Math.floor(log10(Math.abs(bytes)) / log10(1024));
            power = Math.min(power, units.length - 1);

            // round displayed value to one decimal place
            const value = Math.round(bytes / Math.pow(1024, power) * 10) / 10;
            output = `${value} ${units[power]}`;
        }

        return (
            <span>{output}</span>
        );
    }
}

FileSize.propTypes = {
    bytes: oneOfType([number, string])
};

import React, {Component, PropTypes} from 'react';

const { oneOfType, number, string } = PropTypes;
const units = ['bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];

export class FileSize extends Component {
    render() {
        let { bytes } = this.props;
        let output = '-';

        if (typeof bytes == 'string') {
            bytes = parseInt(bytes);
        }

        if (!isNaN(bytes)) {
            let power = Math.floor(Math.log10(bytes) / Math.log10(1024));
            power = Math.min(power, units.length - 1);
            
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

import React, { Component, PropTypes } from 'react';
import SvgSpinner from './SvgSpinner';
import SvgStatus from './SvgStatus';

const { number, string } = PropTypes;

class StatusIndicator extends Component {
    render() {
        const {
            result,
            percentage,
            width = '32px',
            height = '32px',
        } = this.props;
        // early out
        if (!result && !result.toLowerCase) {
            return null;
        }
        const resultClean = result.toLowerCase();
        const props = {
            percentage,
            height,
            width,
            result: resultClean,
            title: resultClean,
        };
        return (resultClean === 'running' || resultClean === 'queued' ? <SvgSpinner
          {...props}
        /> : <SvgStatus
          {...props}
        />);
    }
}

StatusIndicator.propTypes = {
    result: string.isRequired,
    percentage: number,
    width: string,
    height: string,
};

export {
    StatusIndicator,
    SvgSpinner,
    SvgStatus,
};

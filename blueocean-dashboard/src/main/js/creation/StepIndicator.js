/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import { StatusIndicator } from '@jenkins-cd/design-language';
import status from './FlowStatus';

export default function StepIndicator(props) {
    const newProps = {};

    // eslint-disable-next-line
    if (0 <= props.percentage && props.percentage < 100 && props.status !== status.COMPLETE) {
        newProps.result = 'running';
        newProps.percentage = props.percentage;
    } else if (props.status === status.COMPLETE) {
        newProps.result = 'success';
    } else {
        newProps.result = 'not_built';
    }

    return (
        <div className="step-indicator-component">
            <StatusIndicator {...newProps} />
        </div>
    );
}

StepIndicator.propTypes = {
    status: PropTypes.oneOf(status.values()),
    percentage: PropTypes.number,
};

StepIndicator.defaultProps = {
    status: 'incomplete',
    percentage: -1,
};

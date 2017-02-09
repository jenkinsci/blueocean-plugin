import React, { PropTypes } from 'react';
import { StatusIndicator } from '@jenkins-cd/design-language';
import status from './FlowStepStatus';

/**
 * Visual indicator that displays a workflow step as either completed, active, incomplete or error state.
 * Can show partial completion via 'percentage' prop.
 */
export default function StepIndicator(props) {
    const newProps = {};
    newProps.width = newProps.height = 32;

    if (!isNaN(props.percentage) && props.percentage >= 0 && props.status !== status.COMPLETE) {
        newProps.result = 'running';
        newProps.percentage = props.percentage;
    } else if (props.status === status.COMPLETE) {
        newProps.result = 'success';
    } else if (props.status === status.ERROR) {
        newProps.result = 'failure';
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

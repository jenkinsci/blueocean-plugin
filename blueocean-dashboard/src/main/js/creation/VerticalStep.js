/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import StepIndicator from './StepIndicator';
import status from './FlowStatus';

export default function VerticalStep(props) {
    const classNames = `${props.status || ''} ${props.className || ''} ${props.isLastStep ? 'last-step' : ''}`.trim();

    return (
        <div className={`vertical-step-component ${classNames}`}>
            <div className="step-progress">
                <div className="step-stroke-top"></div>
                <StepIndicator status={props.status} percentage={props.percentage} />
                <div className="step-stroke-bottom"></div>
            </div>
            <div className="step-content">
                {props.children}
            </div>
        </div>
    );
}

VerticalStep.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    isLastStep: PropTypes.bool,
    status: PropTypes.oneOf(status.values()),
    percentage: PropTypes.number,
};

VerticalStep.defaultProps = {
    status: 'incomplete',
    percentage: -1,
};

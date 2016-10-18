/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import { StepIndicator } from './StepIndicator';

export function VerticalStep(props) {
    const complete = props.status === 'complete';

    return (
        <div className={`vertical-step-component ${props.status}`}>
            <div className="step-progress">
                <div className="step-stroke-top"></div>
                <StepIndicator complete={complete} />
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
    status: PropTypes.oneOf(['complete', 'active', 'incomplete']),
};

VerticalStep.defaultProps = {
    status: 'incomplete',
};

/**
 * Created by cmeyers on 10/17/16.
 */
import React, { PropTypes } from 'react';
import { StatusIndicator } from '@jenkins-cd/design-language';

export function StepIndicator(props) {
    const result = props.complete ? 'success' : 'not_built';

    return (
        <div className="step-indicator-component">
            <StatusIndicator result={result} />
        </div>
    );
}

StepIndicator.propTypes = {
    complete: PropTypes.bool,
};

StepIndicator.defaultProps = {
    complete: false,
};

import React, { PropTypes } from 'react';
import VerticalStep from './VerticalStep';
import status from './FlowStepStatus';

/**
 * Visual/logic component that defines an individual step of a multi-step workflow.
 * Intended to be used within a MultiStepFlow component.
 * Hides all content except for the title until the step becomes active.
 * Complete the step by calling 'props.onCompleteStep'; complete entire flow by calling 'props.onCompleteFlow'
 */
export default function FlowStep(props) {
    return (
        <VerticalStep
          className={props.className}
          status={props.status}
          percentage={props.percentage}
          isLastStep={props.isLastStep}
        >
            <h1>{props.title}</h1>
            {
                props.status !== status.INCOMPLETE &&
                <fieldset disabled={props.disabled}>
                    {props.children}
                </fieldset>
            }
        </VerticalStep>
    );
}

FlowStep.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
    title: PropTypes.string,
    status: PropTypes.string,
    percentage: PropTypes.number,
    disabled: PropTypes.bool,
    isLastStep: PropTypes.bool,
};

FlowStep.defaultProps = {
    className: '',
};

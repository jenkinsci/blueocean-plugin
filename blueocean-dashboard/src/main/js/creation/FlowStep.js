/**
 * Created by cmeyers on 10/19/16.
 */
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
          status={props.status}
          percentage={props.percentage}
          isLastStep={props.isLastStep}
        >
            <h1>{props.title}</h1>
            {
                props.status !== status.INCOMPLETE &&
                props.children
            }
        </VerticalStep>
    );
}

FlowStep.propTypes = {
    children: PropTypes.node,
    title: PropTypes.string,
    status: PropTypes.string,
    percentage: PropTypes.number,
    isLastStep: PropTypes.bool,
    onCompleteStep: PropTypes.func,
    onCompleteFlow: PropTypes.func,
};

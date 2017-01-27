import React, { PropTypes } from 'react';
import StepStatus from './FlowStepStatus';

/**
 * Used to create a multi-step workflow with one or more FlowStep children.
 * Handles navigating forward through the flow and updating the state of each step.
 */
export default class MultiStepFlow extends React.Component {

    render() {
        const extraClass = this.props.className || '';

        return (
            <div className={`multi-step-flow-component ${extraClass}`}>
                { React.Children.map(this.props.children, (child, index) => {
                    const { activeIndex } = this.props;
                    let status = StepStatus.INCOMPLETE;

                    if (index < activeIndex) {
                        status = StepStatus.COMPLETE;
                    } else if (index === activeIndex) {
                        status = StepStatus.ACTIVE;
                    }

                    const isLastStep = index === this.props.children.length - 1;

                    const extraProps = {
                        status,
                        isLastStep,
                    };

                    return React.cloneElement(child, extraProps);
                })}
            </div>
        );
    }
}

MultiStepFlow.propTypes = {
    className: PropTypes.string,
    children: PropTypes.node,
    activeIndex: PropTypes.number,
    onCompleteFlow: PropTypes.func,
};

MultiStepFlow.defaultProps = {
    activeIndex: 0,
};

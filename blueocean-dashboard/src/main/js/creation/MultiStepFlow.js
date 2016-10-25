/**
 * Created by cmeyers on 10/18/16.
 */
import React, { PropTypes } from 'react';
import StepStatus from './FlowStepStatus';

/**
 * Used to create a multi-step workflow with one or more FlowStep children.
 * Handles navigating forward through the flow and updating the state of each step.
 */
export default class MultiStepFlow extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            currentIndex: 0,
        };
    }

    _onCompleteStep(currentIndex) {
        this.setState({
            currentIndex: currentIndex + 1,
        });
    }

    _onCompleteFlow() {
        this.props.onCompleteFlow();
    }

    render() {
        return (
            <div className="multi-step-flow-component">
                { this.props.children && this.props.children.map((child, index, children) => {
                    const { currentIndex } = this.state;
                    let status = StepStatus.INCOMPLETE;

                    if (index < currentIndex) {
                        status = StepStatus.COMPLETE;
                    } else if (index === currentIndex) {
                        status = StepStatus.ACTIVE;
                    }

                    const isLastStep = index === children.length - 1;

                    const extraProps = {
                        status, isLastStep,
                        onCompleteStep: (step) => this._onCompleteStep(index, step),
                        onCompleteFlow: () => this._onCompleteFlow(),
                    };

                    return React.cloneElement(child, extraProps);
                })}
            </div>
        );
    }
}

MultiStepFlow.propTypes = {
    children: PropTypes.node,
    onCompleteFlow: PropTypes.func,
};

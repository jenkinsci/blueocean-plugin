/**
 * Created by cmeyers on 10/18/16.
 */
import React, { PropTypes } from 'react';
import FlowStatus from './FlowStatus';

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
                    /* eslint-disable */
                    const status =
                        index < currentIndex ? FlowStatus.COMPLETE :
                        index === currentIndex ? FlowStatus.ACTIVE :
                        index > currentIndex ? FlowStatus.INCOMPLETE :
                        null;
                    /* eslint-enable */

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

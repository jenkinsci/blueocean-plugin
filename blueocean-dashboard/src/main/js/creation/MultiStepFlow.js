/**
 * Created by cmeyers on 10/18/16.
 */
import React, { PropTypes } from 'react';
import FlowStatus from './FlowStatus';

export default class MultiStepFlow extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            activeStep: null,
        };
    }

    componentWillMount() {
        this._initialize(this.props);
    }

    componentWillReceiveProps(nextProps) {
        this._initialize(nextProps);
    }

    _initialize(props) {
        if (props.children) {
            const activeStep = props.children[0];

            this.setState({
                activeStep,
            });
        }
    }

    _onCompleteStep() {
        const activeIndex = this.props.children.indexOf(this.state.activeStep);

        if (activeIndex + 1 < this.props.children.length) {
            this.setState({
                activeStep: this.props.children[activeIndex + 1],
            });
        }
    }

    _onCompleteFlow() {
        console.log('flow complete');
        this.props.onCompleteFlow();
    }

    render() {
        return (
            <div className="multi-step-flow-component">
                { this.props.children && this.props.children.map((child, index, children) => {
                    const activeIndex = children.indexOf(this.state.activeStep);
                    /* eslint-disable */
                    const status =
                        index < activeIndex ? FlowStatus.COMPLETE :
                        index === activeIndex ? FlowStatus.ACTIVE :
                        index > activeIndex ? FlowStatus.INCOMPLETE :
                        null;
                    /* eslint-enable */

                    const isLastStep = index === children.length - 1;

                    const extraProps = {
                        status, isLastStep,
                        onCompleteStep: (step) => this._onCompleteStep(step),
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

/**
 * Created by cmeyers on 10/19/16.
 */
import React, { PropTypes } from 'react';
import VerticalStep from './VerticalStep';
import status from './FlowStatus';

export default class FlowStep extends React.Component {

    render() {
        return (
            <VerticalStep
              status={this.props.status}
              percentage={this.props.percentage}
              isLastStep={this.props.isLastStep}
            >
                <h1>{this.props.title}</h1>
                {
                    this.props.status !== status.INCOMPLETE &&
                    this.props.children
                }
            </VerticalStep>
        );
    }

}

FlowStep.propTypes = {
    children: PropTypes.node,
    title: PropTypes.string,
    status: PropTypes.string,
    percentage: PropTypes.number,
    isLastStep: PropTypes.bool,
    onStatusUpdate: PropTypes.func,
    onCompleteStep: PropTypes.func,
};

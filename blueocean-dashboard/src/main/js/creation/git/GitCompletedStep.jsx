/**
 * Created by cmeyers on 10/19/16.
 */
import React, { PropTypes } from 'react';

import FlowStep from '../FlowStep';
import StepStatus from '../FlowStepStatus';

import FlowStatus from './GitCreationStatus';

/**
 * Shows the current progress after creation was initiated.
 */
export default class GitCompletedStep extends React.Component {
    render() {
        let status = this.props.status;
        let percentage = -1;
        let title = 'Completed';
        let content = null;

        switch (this.props.flowStatus) {
        case FlowStatus.CREATE_CREDS:
            percentage = 25;
            title = `${title} - Creating Credentials...`;
            break;
        case FlowStatus.CREATE_PIPELINE:
            percentage = 50;
            title = `${title} - Creating Pipeline...`;
            break;
        case FlowStatus.RUN_PIPELINE:
            percentage = 75;
            title = `${title} - Starting Pipeline...`;
            break;
        case FlowStatus.COMPLETE:
            percentage = 100;
            title = `${title}!`;
            content = <button onClick={() => this.props.onCompleteFlow()}>Close</button>;
            status = StepStatus.COMPLETE;
            break;
        default:
            break;
        }

        return (
            <FlowStep {...this.props} title={title} status={status} percentage={percentage}>
                {content}
            </FlowStep>
        );
    }
}

GitCompletedStep.propTypes = {
    status: PropTypes.string,
    flowStatus: PropTypes.string,
    onCompleteFlow: PropTypes.func,
};

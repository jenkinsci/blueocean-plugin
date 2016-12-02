import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import { buildPipelineUrl } from '../../util/UrlUtils';
import FlowStep from '../FlowStep';
import StepStatus from '../FlowStepStatus';
import FlowStatus from './GitCreationStatus';

/**
 * Shows the current progress after creation was initiated.
 */
@observer
export default class GitCompletedStep extends React.Component {

    finish() {
        const pipeline = this.props.flowManager.pipeline;
        const url = buildPipelineUrl(pipeline.organization, pipeline.fullName, 'activity');
        this.props.flowManager.completeFlow({ url });
    }

    render() {
        let status;
        let percentage = -1;
        let title = 'Completed';
        let content = null;

        switch (this.props.flowManager.creationStatus) {
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
            content = (
                <button onClick={() => this.finish()}>Open</button>
            );
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
    flowManager: PropTypes.string,
};

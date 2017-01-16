import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import { buildPipelineUrl } from '../../util/UrlUtils';
import FlowStep from '../flow2/FlowStep';
import StepStatus from '../flow2/FlowStepStatus';
import FlowStatus from './GitCreationStatus';

let t = null;

/**
 * Shows the current progress after creation was initiated.
 */
@observer
export default class GitCompletedStep extends React.Component {

    constructor(props) {
        super(props);

        t = this.props.flowManager.translate;
    }

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
            percentage = 33;
            title = t('creation.git.step3.title_credential_create');
            break;
        case FlowStatus.CREATE_PIPELINE:
            percentage = 67;
            title = t('creation.git.step3.title_pipeline_create');
            break;
        case FlowStatus.COMPLETE:
            percentage = 100;
            title = t('creation.git.step3.title_completed');
            content = (
                <button onClick={() => this.finish()}>{t('creation.git.step3.button_open')}</button>
            );
            status = StepStatus.COMPLETE;
            break;
        default:
            title = t('creation.git.step3.title_default');
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

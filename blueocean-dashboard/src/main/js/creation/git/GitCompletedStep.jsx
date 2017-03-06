import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import { buildPipelineUrl } from '../../util/UrlUtils';
import FlowStep from '../flow2/FlowStep';
import StepStatus from '../flow2/FlowStepStatus';
import STATE from './GitCreationState';

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
        let title = t('creation.git.step3.title_completed');
        let content = null;

        switch (this.props.flowManager.stateId) {
        case STATE.CREATE_PIPELINE:
            percentage = 50;
            title = t('creation.git.step3.title_pipeline_create');
            break;
        case STATE.COMPLETE:
            percentage = 100;
            content = (
                <button
                  className="button-open-pipeline"
                  onClick={() => this.finish()}
                >
                    {t('creation.git.step3.button_open')}
                </button>
            );
            status = StepStatus.COMPLETE;
            break;
        }

        return (
            <FlowStep {...this.props} className="git-step-completed" title={title} status={status} percentage={percentage}>
                {content}
            </FlowStep>
        );
    }
}

GitCompletedStep.propTypes = {
    flowManager: PropTypes.string,
};

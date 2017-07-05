import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import { buildPipelineUrl } from '../../../../util/UrlUtils';

import FlowStep from '../../../flow2/FlowStep';
import FlowStepStatus from '../../../flow2/FlowStepStatus';
import STATE from '../BbCloudCreationState';

import Extensions from '@jenkins-cd/js-extensions';

@observer
export default class BbCloudCompleteStep extends React.Component {

    navigateDashboard() {
        this.props.flowManager.completeFlow({ url: '/pipelines' });
    }

    navigatePipeline() {
        const { pipeline } = this.props.flowManager;
        const { organization, fullName } = pipeline;
        const url = buildPipelineUrl(organization, fullName, 'activity');
        this.props.flowManager.completeFlow({ url });
    }

    _getStatus(state, status) {
        if (state === STATE.STEP_COMPLETE_SUCCESS) {
            return FlowStepStatus.COMPLETE;
        }

        return status;
    }

    _getTitle(state, repo) {
        if (state === STATE.STEP_COMPLETE_SAVING_ERROR) {
            return 'Error Creating Pipeline';
        } else if (state === STATE.STEP_COMPLETE_EVENT_ERROR) {
            return 'Error Creating Pipeline';
        } else if (state === STATE.STEP_COMPLETE_EVENT_TIMEOUT) {
            return 'Pipeline Creation Pending...';
        } else if (state === STATE.STEP_COMPLETE_MISSING_JENKINSFILE) {
            return <span>There are no Jenkinsfiles in <i>{repo.name}</i></span>;
        } else if (state === STATE.STEP_COMPLETE_SUCCESS) {
            return 'Completed';
        }

        return 'Something Unexpected Happened';
    }

    _getError(state) {
        return state === STATE.STEP_COMPLETE_SAVING_ERROR ||
            state === STATE.STEP_COMPLETE_EVENT_ERROR;
    }

    _getContent(state) {
        const { redirectTimeout, pipelineName } = this.props.flowManager;

        let copy = '';
        let showDashboardLink = false;
        let showCreateLink = false;

        if (state === STATE.STEP_COMPLETE_SAVING_ERROR) {
            copy = 'An error occurrred while saving this pipeline.';
        } else if (state === STATE.STEP_COMPLETE_EVENT_ERROR) {
            copy = 'An error occurred while creating pipelines.';
            showDashboardLink = true;
        } else if (state === STATE.STEP_COMPLETE_EVENT_TIMEOUT) {
            copy = 'Pipelines are still waiting to be created.';
            showDashboardLink = true;
        } else if (state === STATE.STEP_COMPLETE_MISSING_JENKINSFILE) {
            showCreateLink = true;
        } else if (state === STATE.STEP_COMPLETE_SUCCESS) {
            setTimeout(() => this.navigatePipeline(), redirectTimeout);
        }

        return (
            <div>
                <p className="instructions">{copy}</p>

                { showDashboardLink &&
                <div>
                    <p>You may now return to the Dashboard to check for new pipelines.</p>

                    <button onClick={() => this.navigateDashboard()}>Dashboard</button>
                </div>
                }

                { showCreateLink &&
                <div>
                    <Extensions.Renderer extensionPoint="jenkins.pipeline.create.missing.jenkinsfile"
                                         organization={'jenkins'} fullName={pipelineName}
                    />
                </div>
                }
            </div>
        );
    }

    render() {
        const { flowManager } = this.props;
        const status = this._getStatus(flowManager.stateId, this.props.status);
        const error = this._getError(flowManager.stateId);
        const title = this._getTitle(flowManager.stateId, flowManager.selectedRepository);
        const content = this._getContent(flowManager.stateId);

        return (
            <FlowStep {...this.props} className="github-complete-step" title={title} status={status} error={error}>
                {content}
            </FlowStep>
        );
    }
}

BbCloudCompleteStep.propTypes = {
    flowManager: PropTypes.object,
    status: PropTypes.string,
};

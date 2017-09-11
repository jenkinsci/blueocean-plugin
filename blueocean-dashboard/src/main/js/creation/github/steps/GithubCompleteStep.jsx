import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import { buildPipelineUrl } from '../../../util/UrlUtils';

import FlowStep from '../../flow2/FlowStep';
import FlowStepStatus from '../../flow2/FlowStepStatus';
import STATE from '../GithubCreationState';

import Extensions from '@jenkins-cd/js-extensions';
let t = null;

@observer
export default class GithubCompleteStep extends React.Component {

    componentWillMount() {
        t = this.props.flowManager.translate;
    }

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
        if (state === STATE.PENDING_CREATION_SAVING || state === STATE.PENDING_CREATION_EVENTS) {
            return t('creation.bitbucket.pending.title');
        } else if (state === STATE.STEP_COMPLETE_SAVING_ERROR) {
            return t('creation.error.creating_pipeline');
        } else if (state === STATE.STEP_COMPLETE_EVENT_ERROR) {
            return t('creation.core.error.creating.pipeline');
        } else if (state === STATE.STEP_COMPLETE_EVENT_TIMEOUT) {
            return t('creation.core.status.pending');
        } else if (state === STATE.STEP_COMPLETE_MISSING_JENKINSFILE) {
            return <span>{t('creation.core.error.missing.jenkinsfile')} <i>{repo.name}</i></span>;
        } else if (state === STATE.STEP_COMPLETE_SUCCESS) {
            return t('creation.core.status.completed');
        }

        return t('creation.core.error.unexpected');
    }

    _getError(state) {
        return state === STATE.STEP_COMPLETE_EVENT_ERROR;
    }

    _getContent(state) {
        const { redirectTimeout, pipelineName } = this.props.flowManager;

        let copy = '';
        let showDashboardLink = false;
        let showCreateLink = false;

        if (state === STATE.STEP_COMPLETE_EVENT_ERROR) {
            copy = t('creation.core.error.creating.pipeline');
            showDashboardLink = true;
        } else if (state === STATE.STEP_COMPLETE_EVENT_TIMEOUT) {
            copy = t('creation.core.status.waiting');
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
                    <p>{t('creation.core.status.return.new_pipelines')}.</p>

                    <button onClick={() => this.navigateDashboard()}>{t('creation.core.button.dashboard')}</button>
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
        const loading = (flowManager.stateId === STATE.PENDING_CREATION_SAVING ||
            flowManager.stateId === STATE.PENDING_CREATION_EVENTS);

        return (
            <FlowStep {...this.props} className="github-complete-step" title={title} status={status} loading={loading} error={error}>
                {content}
            </FlowStep>
        );
    }
}

GithubCompleteStep.propTypes = {
    flowManager: PropTypes.object,
    status: PropTypes.string,
};

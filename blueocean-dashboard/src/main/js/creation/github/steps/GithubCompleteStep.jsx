import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';
import FlowStepStatus from '../../flow2/FlowStepStatus';
import STATE from '../GithubCreationState';

@observer
export default class GithubCompleteStep extends React.Component {

    finish() {
        this.props.flowManager.completeFlow({ url: '/pipelines' });
    }

    _getStatus(state, status) {
        if (state === STATE.STEP_COMPLETE_SUCCESS) {
            return FlowStepStatus.COMPLETE;
        }

        return status;
    }

    _getTitle(state, autoDiscover) {
        if (state === STATE.PENDING_CREATION_SAVING) {
            return autoDiscover ? 'Saving Organization...' : 'Creating Pipeline...';
        } else if (state === STATE.STEP_COMPLETE_SAVING_ERROR) {
            return 'Error Saving Organization';
        } else if (state === STATE.PENDING_CREATION_EVENTS) {
            return autoDiscover ? 'Creating Pipelines...' : 'Creating Pipeline...';
        } else if (state === STATE.STEP_COMPLETE_EVENT_ERROR) {
            return autoDiscover ? 'Error Creating Pipelines' : 'Error Creating Pipeline';
        } else if (state === STATE.STEP_COMPLETE_EVENT_TIMEOUT) {
            return 'Pipeline Creation Pending...';
        } else if (state === STATE.STEP_COMPLETE_SUCCESS) {
            return 'Creation Successful!';
        }

        return 'Something Unexpected Happened';
    }

    _getLoading(state) {
        return state === STATE.PENDING_CREATION_SAVING ||
            state === STATE.PENDING_CREATION_EVENTS;
    }

    _getError(state) {
        return state === STATE.STEP_COMPLETE_SAVING_ERROR ||
                state === STATE.STEP_COMPLETE_EVENT_ERROR;
    }

    _getContent(state, count) {
        let copy = '';
        let showLink = false;

        if (state === STATE.PENDING_CREATION_SAVING) {
            copy = 'Please wait while your settings are saved.';
        } else if (state === STATE.STEP_COMPLETE_SAVING_ERROR) {
            copy = 'An error occurrred while saving this pipeline.';
        } else if (state === STATE.PENDING_CREATION_EVENTS) {
            copy = `Saving was successful. Pipeline creation is in progress. ${count} pipelines have been created.`;
        } else if (state === STATE.STEP_COMPLETE_EVENT_ERROR) {
            copy = 'An error occurred while discovering pipelines.';
            showLink = true;
        } else if (state === STATE.STEP_COMPLETE_EVENT_TIMEOUT) {
            copy = 'Pipelines are still waiting to be created.';
            showLink = true;
        } else if (state === STATE.STEP_COMPLETE_SUCCESS) {
            copy = `Success! ${count} pipelines have been created.`;
            showLink = true;
        }

        return (
            <div>
                <p className="instructions">{copy}</p>

                { showLink && <p>You may now return to the Dashboard to check for new pipelines.</p> }

                { showLink && <button onClick={() => this.finish()}>Dashboard</button> }
            </div>
        );
    }

    render() {
        const { flowManager } = this.props;
        const status = this._getStatus(flowManager.stateId, this.props.status);
        const loading = this._getLoading(flowManager.stateId);
        const error = this._getError(flowManager.stateId);
        const title = this._getTitle(flowManager.stateId, flowManager.selectedAutoDiscover);
        const content = this._getContent(flowManager.stateId, flowManager.pipelineCount);


        return (
            <FlowStep {...this.props} className="github-complete-step" title={title} status={status} loading={loading} error={error}>
                {content}
            </FlowStep>
        );
    }

}

GithubCompleteStep.propTypes = {
    flowManager: PropTypes.object,
};

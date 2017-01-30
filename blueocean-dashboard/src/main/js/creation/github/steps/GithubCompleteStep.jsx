import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';
import STATE from '../GithubCreationState';

@observer
export default class GithubCompleteStep extends React.Component {

    _getTitle(state) {
        if (state === STATE.PENDING_CREATION_SAVING) {
            return 'Saving Organization...';
        } else if (state === STATE.STEP_COMPLETE_SAVING_ERROR) {
            return 'Error Saving Organization';
        } else if (state === STATE.PENDING_CREATION_EVENTS) {
            return 'Creating Pipelines...';
        } else if (state === STATE.STEP_COMPLETE_EVENT_ERROR) {
            return 'Error Creating Pipelines';
        } else if (state === STATE.STEP_COMPLETE_SUCCESS) {
            return 'Creation Successful!';
        }

        return 'Something Unexpected Happened';
    }

    _getLoading(state) {
        return state === STATE.PENDING_CREATION_SAVING ||
            state === STATE.PENDING_CREATION_EVENTS;
    }

    _getContent(state) {
        return state;
    }

    render() {
        const { flowManager } = this.props;
        const loading = this._getLoading(flowManager.stateId);
        const title = this._getTitle(flowManager.stateId);
        const content = this._getContent(flowManager.stateId);

        return (
            <FlowStep {...this.props} title={title} loading={loading}>
                {content}
            </FlowStep>
        );
    }

}

GithubCompleteStep.propTypes = {
    flowManager: PropTypes.object,
};

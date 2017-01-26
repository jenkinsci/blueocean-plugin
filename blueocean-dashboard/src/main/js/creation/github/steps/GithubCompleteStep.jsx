import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';
import STATUS from '../GithubCreationStatus';

@observer
export default class GithubCompleteStep extends React.Component {

    _getTitle(status) {
        if (status === STATUS.PENDING_CREATION_SAVING) {
            return 'Saving Organization...';
        } else if (status === STATUS.STEP_COMPLETE_SAVING_ERROR) {
            return 'Error Saving Organization';
        } else if (status === STATUS.PENDING_CREATION_EVENTS) {
            return 'Creating Pipelines...';
        } else if (status === STATUS.STEP_COMPLETE_EVENT_ERROR) {
            return 'Error Creating Pipelines';
        } else if (status === STATUS.STEP_COMPLETE_SUCCESS) {
            return 'Creation Successful!';
        }

        return 'Something Unexpected Happened';
    }

    _getLoading(status) {
        return status === STATUS.PENDING_CREATION_SAVING ||
            status === STATUS.PENDING_CREATION_EVENTS;
    }

    _getContent(status) {
        return status;
    }

    render() {
        const { flowManager } = this.props;
        const loading = this._getLoading(flowManager.status);
        const title = this._getTitle(flowManager.status);
        const content = this._getContent(flowManager.status);

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

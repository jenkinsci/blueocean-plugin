import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';
import STATUS from '../GithubCreationStatus';

@observer
export default class GithubCompleteStep extends React.Component {

    render() {
        const { flowManager } = this.props;

        const title = flowManager.status === STATUS.PENDING_CREATION ?
            'Creating...' : 'Coming Soon!';

        return (
            <FlowStep {...this.props} title={title}>
                complete
            </FlowStep>
        );
    }

}

GithubCompleteStep.propTypes = {
    flowManager: PropTypes.object,
};

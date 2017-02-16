import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubAlreadyDiscoverStep extends React.Component {

    _exit() {
        this.props.flowManager.completeFlow();
    }

    render() {
        const { flowManager } = this.props;
        const title = 'Already Discovering';

        return (
            <FlowStep {...this.props} className="github-already-discover-step" title={title}>
                <p className="instructions">
                    The organization {flowManager.selectedOrganization.name} is already set to actively
                    search for new repositories that contain Jenkinsfiles.
                </p>

                <p>You may make a different selection above or exit.</p>

                <button onClick={() => this._exit()}>Exit</button>
            </FlowStep>
        );
    }

}

GithubAlreadyDiscoverStep.propTypes = {
    flowManager: PropTypes.object,
};

import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubConfirmDiscoverStep extends React.Component {

    confirmDiscover() {
        this.props.flowManager.saveAutoDiscover();
    }

    render() {
        const { flowManager } = this.props;
        const title = 'Create Pipelines';
        const buttonLabel = 'Create Pipelines';
        const showWarning = flowManager.existingOrgFolder && !flowManager.existingAutoDiscover;

        return (
            <FlowStep {...this.props} className="github-confirm-discover-step" title={title}>
                { !showWarning &&
                <p className="instructions">
                    When this option is selected, Jenkins will actively search for new repositories
                    in {flowManager.selectedOrganization.name} that contain Jenkinsfiles and
                    create Pipelines for them.
                </p>
                }

                { showWarning &&
                <div>
                    <p className="instructions">
                        By changing "{flowManager.selectedOrganization.name}" to "Automatically discover",
                        the current {flowManager.existingPipelineCount} pipelines will be preserved.
                    </p>

                    <p className="instructions">
                        Jenkins will actively search for new repositories that contain Jenkinsfiles and
                        create Pipelines for them.
                    </p>
                </div>
                }

                <button onClick={() => this.confirmDiscover()}>{buttonLabel}</button>
            </FlowStep>
        );
    }

}

GithubConfirmDiscoverStep.propTypes = {
    flowManager: PropTypes.object,
};

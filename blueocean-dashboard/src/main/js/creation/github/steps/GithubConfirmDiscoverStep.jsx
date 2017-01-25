import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';

import FlowStep from '../../flow2/FlowStep';

@observer
export default class GithubConfirmDiscoverStep extends React.Component {

    confirmDiscover() {
        this.props.flowManager.confirmDiscover();
    }

    render() {
        const { flowManager } = this.props;
        const title = 'Create Pipelines';
        const buttonLabel = `Create ${flowManager.selectableRepositories.length} Repositories`;

        return (
            <FlowStep {...this.props} className="github-confirm-discover-step" title={title}>
                <div>
                    <p className="instructions">
                        When this option is selected, Jenkins will actively search for new repositories
                        in {flowManager.selectedOrganization.name} that contains Jenkinsfiles and
                        create Pipelines for them.
                    </p>

                    <button onClick={() => this.confirmDiscover()}>{buttonLabel}</button>
                </div>
            </FlowStep>
        );
    }

}

GithubConfirmDiscoverStep.propTypes = {
    flowManager: PropTypes.object,
};

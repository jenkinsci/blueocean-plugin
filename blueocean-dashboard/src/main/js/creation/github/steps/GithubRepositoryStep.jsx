import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { List } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import STATUS from '../GithubCreationStatus';

@observer
export default class GithubRepositoryStep extends React.Component {

    selectRepository(org) {
        this.props.flowManager.selectRepository(org);
    }

    beginCreation() {
        this.props.flowManager.createFromRepository();
    }

    render() {
        const { flowManager } = this.props;
        const loaded = flowManager.status === STATUS.STEP_CHOOSE_REPOSITORY;
        const disabled = !flowManager.selectedRepository;
        const title = loaded ? 'Choose a repository' : 'Loading Repositories...';
        // touch 'repositories' to ensure that changes to it will trigger the @observer
        flowManager.repositories.slice();

        return (
            <FlowStep {...this.props} className="github-repo-list-step" title={title}>
                { loaded &&
                <div className="container">
                    <List
                      className="repo-list"
                      data={flowManager.repositories}
                      onItemSelect={(idx, repo) => this.selectRepository(repo)}
                      labelFunction={repo => repo.name}
                    />

                    <button
                      className="button-create"
                      onClick={() => this.beginCreation()}
                      disabled={disabled}
                    >
                        Create Pipeline
                    </button>
                </div>
                }
            </FlowStep>
        );
    }
}

GithubRepositoryStep.propTypes = {
    flowManager: PropTypes.object,
};

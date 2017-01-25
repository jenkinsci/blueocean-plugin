import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { List } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';

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
        const buttonDisabled = !flowManager.selectedRepository;
        const title = 'Choose a repository';

        return (
            <FlowStep {...this.props} className="github-repo-list-step" title={title}>
                <div className="container">
                    <List
                      className="repo-list"
                      data={flowManager.selectableRepositories}
                      onItemSelect={(idx, repo) => this.selectRepository(repo)}
                      labelFunction={repo => repo.name}
                    />

                    <button
                      className="button-create"
                      onClick={() => this.beginCreation()}
                      disabled={buttonDisabled}
                    >
                        Create Pipeline
                    </button>
                </div>
            </FlowStep>
        );
    }
}

GithubRepositoryStep.propTypes = {
    flowManager: PropTypes.object,
};

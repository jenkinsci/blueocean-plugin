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
        this.props.flowManager.saveSingleRepo();
    }

    _exit() {
        this.props.flowManager.completeFlow();
    }

    render() {
        const { flowManager } = this.props;
        const buttonDisabled = !flowManager.selectedRepository;
        const title = 'Choose a repository';

        return (
            <FlowStep {...this.props} className="github-repo-list-step" title={title}>
                { flowManager.selectableRepositories.length > 0 &&
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
                }

                { flowManager.repositories.length > 0 && flowManager.selectableRepositories.length === 0 &&
                <div className="container">
                    <p className="instructions">
                        All {flowManager.repositories.length} discovered repositories in the organization
                        "{flowManager.selectedOrganization.name}" already have Pipelines.
                    </p>

                    <button onClick={() => this._exit()}>Exit</button>
                </div>
                }

                { flowManager.repositories.length === 0 &&
                <div className="container">
                    <p className="instructions">
                        The organization "{flowManager.selectedOrganization.name}" has no repositories.

                        Please pick a different organization or choose "Automatically Discover" instead.
                    </p>

                    <button onClick={() => this._exit()}>Exit</button>
                </div>
                }
            </FlowStep>
        );
    }
}

GithubRepositoryStep.propTypes = {
    flowManager: PropTypes.object,
};

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

    _sortRepos(a, b) {
        return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
    }

    render() {
        const { flowManager } = this.props;
        const title = 'Choose a repository';
        const disabled = flowManager.stepsDisabled;
        const buttonDisabled = !flowManager.selectedRepository;
        const orgName = flowManager.selectedOrganization.name;
        const existingPipelineCount = flowManager.existingPipelineCount;
        const sortedRepos = flowManager.selectableRepositories.slice().sort(this._sortRepos);

        return (
            <FlowStep {...this.props} className="github-repo-list-step" title={title} disabled={disabled}>
                { flowManager.existingAutoDiscover &&
                <div>
                    <p className="instructions">
                        The organization "{orgName}"is currently set to "Automatically discover."
                        Changing to "Just one repository" will create one new pipeline and
                        preserve the existing {existingPipelineCount} pipelines.
                    </p>

                    <p className="instructions">
                        Jenkins will no longer actively search for new repositories that contain Jenkinsfiles
                        and create Pipelines for them.
                    </p>
                </div>
                }

                { flowManager.selectableRepositories.length > 0 &&
                <div className="container">
                    <List
                      className="repo-list"
                      data={sortedRepos}
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
                        "{orgName}" already have Pipelines.
                    </p>

                    <button onClick={() => this._exit()}>Exit</button>
                </div>
                }

                { flowManager.repositories.length === 0 &&
                <div className="container">
                    <p className="instructions">
                        The organization "{orgName}" has no repositories.

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

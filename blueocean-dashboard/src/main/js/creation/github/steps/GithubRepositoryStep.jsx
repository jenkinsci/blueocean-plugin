import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { List } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import STATUS from '../GithubCreationStatus';

@observer
export default class GithubOrgListStep extends React.Component {

    selectRepository(org) {
        this.props.flowManager.selectRepository(org);
    }

    render() {
        const { flowManager } = this.props;

        const title = flowManager.status === STATUS.PENDING_LOADING_REPOSITORIES ?
            'Loading Repositories...' : 'Choose a repository';

        return (
            <FlowStep {...this.props} className="github-repo-list-step" title={title}>
                <List
                  className="repo-list"
                  data={flowManager.repositories}
                  onItemSelect={(idx, repo) => this.selectRepository(repo)}
                  labelFunction={repo => repo.name}
                />
            </FlowStep>
        );
    }
}

GithubOrgListStep.propTypes = {
    flowManager: PropTypes.object,
};

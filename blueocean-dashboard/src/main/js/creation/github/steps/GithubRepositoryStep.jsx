import React, { PropTypes } from 'react';
import FlowStep from '../../flow2/FlowStep';
import { observer } from 'mobx-react';

@observer
export default class GithubOrgListStep extends React.Component {

    selectRepository(org) {
        this.props.flowManager.selectRepository(org);
    }

    render() {
        const { flowManager } = this.props;

        return (
            <FlowStep {...this.props} title="Choose a repository">
                {flowManager.repos.map(repo => (
                    <div>
                        <button onClick={() => this.selectRepository(repo)}>{repo.name}</button>
                    </div>
                ))}
            </FlowStep>
        );
    }
}

GithubOrgListStep.propTypes = {
    flowManager: PropTypes.object,
};

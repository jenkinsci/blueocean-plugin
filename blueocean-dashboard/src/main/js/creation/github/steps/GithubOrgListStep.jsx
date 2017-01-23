/**
 * Created by cmeyers on 11/30/16.
 */
import React, { PropTypes } from 'react';
import FlowStep from '../../flow2/FlowStep';
import { observer } from 'mobx-react';

@observer
export default class GithubOrgListStep extends React.Component {
    render() {
        return (
            <FlowStep {...this.props} title="In which Github organization are your repositories located?">
                {this.props.flowManager.organizations.map(org => (
                    <div>{org.name}</div>
                ))}
            </FlowStep>
        );
    }
}

GithubOrgListStep.propTypes = {
    flowManager: PropTypes.object,
};

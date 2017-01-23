import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { List } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';


function OrgRenderer(props) {
    const { listItem } = props;
    const { name, avatar } = listItem;

    return (
        <div className="org-list-item">
            <img className="avatar" width="30" height="30" src={`${avatar}&s=50`} />
            <span>{name}</span>
        </div>
    );
}

OrgRenderer.propTypes = {
    listItem: PropTypes.object,
};


@observer
export default class GithubOrgListStep extends React.Component {

    selectOrganization(org) {
        this.props.flowManager.selectOrganization(org);
    }

    render() {
        const { flowManager } = this.props;
        const title = 'In which Github organization are your repositories located?';

        return (
            <FlowStep {...this.props} className="github-org-list-step layout-large" title={title}>
                <List
                  className="org-list"
                  data={flowManager.organizations}
                  onItemSelect={(idx, org) => this.selectOrganization(org)}
                  defaultContainerClass={false}
                >
                    <OrgRenderer />
                </List>
            </FlowStep>
        );
    }
}

GithubOrgListStep.propTypes = {
    flowManager: PropTypes.object,
};

import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { List } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');

function OrgRenderer(props) {
    const { listItem } = props;
    const { name, avatar } = listItem;

    return (
        <div className="org-list-item">
            <img className="avatar" width="30" height="30" src={`${avatar}`} />
            <span>{name}</span>
        </div>
    );
}

OrgRenderer.propTypes = {
    listItem: PropTypes.object,
};


@observer
export default class BbOrgListStep extends React.Component {

    selectOrganization(org) {
        this.props.flowManager.selectOrganization(org);
    }

    render() {
        const {
            flowManager,
            title = 'creation.bbcloud.repository.title',
        } = this.props;

        const titleString = t(title);
        const disabled = flowManager.stepsDisabled;

        return (
            <FlowStep {...this.props} className="github-org-list-step layout-large" title={titleString} disabled={disabled}>
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

BbOrgListStep.propTypes = {
    flowManager: PropTypes.object,
    title: PropTypes.string,
};

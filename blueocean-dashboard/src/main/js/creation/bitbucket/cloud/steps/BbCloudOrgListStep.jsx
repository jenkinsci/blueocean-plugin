import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { List } from '@jenkins-cd/design-language';

import FlowStep from '../../../flow2/FlowStep';
import { i18nTranslator, Fetch, UrlConfig, AppConfig } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');
import { Button } from '../../../github/Button';

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

    constructor(props) {
        super(props);
        this.state = {
            errorMessage: null,
            installResult: null,
        };
    }

    selectOrganization(org) {
        this.props.flowManager.selectOrganization(org);
    }

    _installAddOn() {
        this.setState({
            installResult: 'running',
        });
        Fetch.fetchJSON(`${UrlConfig.getJenkinsRootURL()}/blue/rest/organizations/${AppConfig.getOrganizationName()}/bitbucket-connect/connect`)
            .then(
                (data) => {
                    this.setState({
                        installResult: 'success',
                    });
                    window.location.replace(data.redirectUrl);
                },
                error => this.setState({
                    errorMessage: error,
                })
            );
    }


    render() {
        const { flowManager } = this.props;
        const title = t('creation.core.repository.title');
        const disabled = flowManager.stepsDisabled;

        const connectTitle = t('creation.bitbucket.team.connect');
        const result = this.state.installResult;
        const status = {
            result,
        };


        return (
            <FlowStep {...this.props} className="github-org-list-step layout-large" title={title} disabled={disabled}>
                <List
                    className="org-list"
                    data={flowManager.organizations}
                    onItemSelect={(idx, org) => this.selectOrganization(org)}
                    defaultContainerClass={false}
                >
                    <OrgRenderer />
                </List>
                <p className="instructions">
                    {connectTitle} &nbsp;
                </p>
                <Button className="button-create-credental" status={status} onClick={() => this._installAddOn()}>Connect</Button>
            </FlowStep>);
    }
}

BbOrgListStep.propTypes = {
    flowManager: PropTypes.object,
};

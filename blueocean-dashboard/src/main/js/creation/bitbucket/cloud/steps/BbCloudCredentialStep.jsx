import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import FlowStep from '../../../flow2/FlowStep';
import { Button } from '../../../github/Button';
import { i18nTranslator, Fetch, UrlConfig, AppConfig } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');

@observer
export default class BbCredentialsStep extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            errorMessage: null,
            installResult: null,
        };
    }

    _createCredential() {
        const valid = this._performValidation();
        if (!valid) {
            return;
        }
        this.props.flowManager.createCredential(this.state.usernameValue, this.state.passwordValue);
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
        const title = t('creation.bitbucket.team.connect');
        const result = this.state.installResult;
        const status = {
            result,
        };
        return (

            <FlowStep {...this.props} className="github-credentials-step" title={title}>
                <p className="instructions">
                    Connect your Bitbucket Cloud account to Jenkins. &nbsp;
                </p>
                <Button className="button-create-credental" status={status} onClick={() => this._installAddOn()}>Connect</Button>
            </FlowStep>
        );
    }
}

BbCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
};

import React, { PropTypes } from 'react';
import Extensions from '@jenkins-cd/js-extensions';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';

import FlowStep from '../../flow2/FlowStep';

const t = i18nTranslator('blueocean-dashboard');

export default class GithubCredentialsStep extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            loading: false,
            complete: false,
        };
    }

    _onStatus(status) {
        const loading = status === 'promptLoading';

        this.setState({
            loading,
        });
    }

    _onComplete(credential, selectionType) {
        this.setState({
            complete: true,
        });

        if (this.props.onCredentialSelected) {
            this.props.onCredentialSelected(credential, selectionType);
        }
    }

    render() {
        const scmId = this.props.flowManager.getScmId();
        const loading = this.state.loading;
        const disabled = this.state.complete;
        const title = loading ? t('common.pager.loading', { defaultValue: 'Loading...' }) : 'Connect to GitHub';

        const githubConfig = {
            scmId,
            apiUrl: this.props.flowManager.getApiUrl(),
        };

        return (
            <FlowStep {...this.props} className="github-credentials-step" disabled={disabled} loading={loading} title={title}>
                <Extensions.Renderer
                    extensionPoint="jenkins.credentials.selection"
                    onStatus={status => this._onStatus(status)}
                    onComplete={(credential, selectionType) => this._onComplete(credential, selectionType)}
                    type={scmId}
                    githubConfig={githubConfig}
                />
            </FlowStep>
        );
    }
}

GithubCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
    onCredentialSelected: PropTypes.func,
};

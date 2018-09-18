import React, { PropTypes } from 'react';
import CredentialsPicker from '../../../credentials/CredentialsPicker';

import FlowStep from '../../flow2/FlowStep';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');

export default class BbCredentialsStep extends React.Component {
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
        const title = t('creation.bitbucket.connect');

        const scmSource = {
            id: scmId,
            apiUrl: this.props.flowManager.getApiUrl(),
        };

        return (
            <FlowStep {...this.props} className="bitbucket-credentials-step" disabled={disabled} loading={loading} title={title}>
                <CredentialsPicker
                    onStatus={status => this._onStatus(status)}
                    onComplete={(credential, selectionType) => this._onComplete(credential, selectionType)}
                    type={scmId}
                    scmSource={scmSource}
                />
            </FlowStep>
        );
    }
}

BbCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
    onCredentialSelected: PropTypes.func,
};

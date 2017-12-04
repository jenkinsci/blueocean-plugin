import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { FormElement, PasswordInput } from '@jenkins-cd/design-language';

import { Button } from '../../creation/github/Button';
import GithubApiUtils from '../../creation/github/api/GithubApiUtils';

import GithubCredentialsManager from './GithubCredentialsManager';
import GithubCredentialsState from './GithubCredentialsState';

function getCreateTokenUrl(apiUrl) {
    let baseUrl = 'https://github.com';

    // will default to above for blank value or api.github.com usages
    if (apiUrl && apiUrl.indexOf('https://api.github.com') !== 0) {
        baseUrl = GithubApiUtils.extractProtocolHost(apiUrl);
    }

    return `${baseUrl}/settings/tokens/new?scopes=repo,read:user,user:email,write:repo_hook`;
}

@observer
class GithubCredentialsPicker extends React.Component {
    constructor(props) {
        super(props);

        this.tokenManager = new GithubCredentialsManager();

        this.state = {
            loading: false,
            accessToken: '',
        };
    }

    componentWillMount() {
        this.setState({
            loading: true,
        });

        if (this.props.onStatus) {
            this.props.onStatus('promptLoading');
        }
    }

    componentDidMount() {
        this._configure(this.props);
        this.tokenManager.findExistingCredential().then(credential => this._findExistingCredentialComplete(credential));
    }

    _configure(props) {
        this.tokenManager.configure(props.scmId, props.apiUrl);
    }

    _findExistingCredentialComplete(credential) {
        this.setState({
            loading: false,
        });

        if (credential && this.props.onComplete) {
            this.props.onComplete(credential, 'autoSelected');
        } else if (this.props.onStatus) {
            this.props.onStatus('promptReady');
        }
    }

    _tokenChange(accessToken) {
        const trimmed = typeof accessToken === 'string' ? accessToken.trim() : accessToken;
        this.setState({
            accessToken: trimmed,
        });
    }

    _createToken() {
        this.tokenManager.createAccessToken(this.state.accessToken).then(credential => this._onCreateTokenSuccess(credential));
    }

    _onCreateTokenSuccess(credential) {
        if (credential && this.props.onComplete) {
            this.props.onComplete(credential, 'userSelected');
        }
    }

    _getErrorMessage(stateId) {
        if (stateId === GithubCredentialsState.EXISTING_REVOKED) {
            return 'The existing access token appears to have been deleted. Please create a new token.';
        } else if (stateId === GithubCredentialsState.EXISTING_MISSING_SCOPES) {
            return 'The existing access token is missing the required scopes. Please create a new token.';
        } else if (stateId === GithubCredentialsState.VALIDATION_FAILED_TOKEN) {
            return 'Invalid access token.';
        } else if (stateId === GithubCredentialsState.VALIDATION_FAILED_SCOPES) {
            return 'Access token must have the following scopes: "repos" and "user:email"';
        }

        return null;
    }

    render() {
        const errorMessage = this._getErrorMessage(this.tokenManager.stateId);
        const tokenUrl = getCreateTokenUrl(this.props.apiUrl);

        let result = null;

        if (this.tokenManager.pendingValidation) {
            result = 'running';
        } else if (this.tokenManager.stateId === GithubCredentialsState.SAVE_SUCCESS) {
            result = 'success';
        }

        const status = {
            result,
        };

        return (
            !this.state.loading && (
                <div className="credentials-picker-github">
                    <p className="instructions">
                        Jenkins needs an access key to authorize itself with Github. <br />
                        <a href={tokenUrl} target="_blank">
                            Create an access key here.
                        </a>
                    </p>

                    <FormElement errorMessage={errorMessage}>
                        <PasswordInput className="text-token" placeholder="Your Github access token" onChange={val => this._tokenChange(val)} />

                        <Button className="button-connect" status={status} onClick={() => this._createToken()}>
                            Connect
                        </Button>
                    </FormElement>
                </div>
            )
        );
    }
}

GithubCredentialsPicker.propTypes = {
    onStatus: PropTypes.func,
    onComplete: PropTypes.func,
    scmId: PropTypes.string,
    apiUrl: PropTypes.string,
};

export default GithubCredentialsPicker;

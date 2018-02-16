import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import { FormElement, PasswordInput, TextInput } from '@jenkins-cd/design-language';

import BbCredentialsManager from './BbCredentialsManager';
import BbCredentialsState from './BbCredentialsState';
import { Button } from '../../creation/github/Button';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');

@observer
class BbCredentialsPicker extends React.Component {

    constructor(props) {
        super(props);

        this.credentialsManager = new BbCredentialsManager();

        this.state = {
            loading: false,
            usernameValue: null,
            usernameErrorMsg: null,
            passwordValue: null,
            passwordErrorMsg: null,
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
        this.credentialsManager.findExistingCredential()
            .then(credential => this._findExistingCredentialComplete(credential));
    }

    _configure(props) {
        this.credentialsManager.configure(props.scmId, props.apiUrl);
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

    _createCredential() {
        const valid = this._performValidation();
        if (!valid) {
            return;
        }
        this.credentialsManager.createCredential(this.state.usernameValue, this.state.passwordValue)
            .then(credential => this._onCreateCredentialSuccess(credential));
    }

    _onCreateCredentialSuccess(credential) {
        if (credential && this.props.onComplete) {
            this.props.onComplete(credential, 'userSelected');
        }
    }

    _getErrorMessage(stateId) {
        if (stateId === BbCredentialsState.INVALID_CREDENTIAL) {
            return t('creation.bitbucket.connect.invalid_username_password');
        } else if (stateId === BbCredentialsState.REVOKED_CREDENTIAL) {
            return t('creation.bitbucket.connect.revoked_credential');
        } else if (stateId === BbCredentialsState.UNEXPECTED_ERROR_CREDENTIAL) {
            return t('creation.bitbucket.connect.unexpected_error');
        }
        return null;
    }

    _performValidation() {
        let result = true;
        if (!this.state.usernameValue) {
            this.setState({
                usernameErrorMsg: t('creation.git.create_credential.username_error'),
            });

            result = false;
        }

        if (!this.state.passwordValue) {
            this.setState({
                passwordErrorMsg: t('creation.git.create_credential.password_error'),
            });

            result = false;
        }
        return result;
    }

    _usernameChange(value) {
        this.setState({
            usernameValue: value,
        });

        this._updateUsernameErrorMsg();
    }

    _updateUsernameErrorMsg = debounce(reset => {
        if (reset || (this.state.usernameErrorMsg && this.state.usernameValue)) {
            this.setState({
                usernameErrorMsg: null,
            });
        }
    }, 200);

    _passwordChange(value) {
        this.setState({
            passwordValue: value,
        });

        this._updatePasswordErrorMsg();
    }

    _updatePasswordErrorMsg = debounce(reset => {
        if (reset || (this.state.passwordErrorMsg && this.state.passwordValue)) {
            this.setState({
                passwordErrorMsg: null,
            });
        }
    }, 200);

    render() {
        const errorMessage = this._getErrorMessage(this.credentialsManager.stateId);

        let result = null;

        if (this.credentialsManager.pendingValidation) {
            result = 'running';
        } else if (this.credentialsManager.stateId === BbCredentialsState.SAVE_SUCCESS) {
            result = 'success';
        }

        const status = {
            result,
        };

        return (
            !this.state.loading &&
            <div className="credentials-picker-bitbucket">
                <p className="instructions">
                    {t('creation.bitbucket.connect.authorize')}. &nbsp;
                </p>
                <FormElement
                    className="credentials-new"
                    errorMessage={errorMessage}
                    verticalLayout
                >
                    <FormElement title={t('creation.git.create_credential.username_title')} errorMessage={this.state.usernameErrorMsg}>
                        <TextInput className="text-username" onChange={val => this._usernameChange(val)} />
                    </FormElement>
                    <FormElement title={t('creation.git.create_credential.password_title')} errorMessage={this.state.passwordErrorMsg}>
                        <PasswordInput className="text-password" onChange={val => this._passwordChange(val)} />
                    </FormElement>
                </FormElement>
                <Button className="button-create-credental" status={status} onClick={() => this._createCredential()}>Connect</Button>
            </div>
        );
    }
}

BbCredentialsPicker.propTypes = {
    onStatus: PropTypes.func,
    onComplete: PropTypes.func,
    scmId: PropTypes.string,
    apiUrl: PropTypes.string,
};

export default BbCredentialsPicker;

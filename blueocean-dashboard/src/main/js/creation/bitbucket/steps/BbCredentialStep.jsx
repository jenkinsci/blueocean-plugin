import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import { FormElement, PasswordInput, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import { BbCredentialState } from '../BbCredentialState';
import { Button } from '../../github/Button';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
const t = i18nTranslator('blueocean-dashboard');

@observer
export default class BbCredentialsStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            usernameValue: null,
            usernameErrorMsg: null,
            passwordValue: null,
            passwordErrorMsg: null,
        };
    }

    _createCredential() {
        const valid = this._performValidation();
        if (!valid) {
            return;
        }
        this.props.flowManager.createCredential(this.state.usernameValue, this.state.passwordValue);
    }

    _getErrorMessage(stateId) {
        if (stateId === BbCredentialState.INVALID_CREDENTIAL) {
            return t('creation.bitbucket.connect.invalid_username_password');
        } else if (stateId === BbCredentialState.UNEXPECTED_ERROR_CREDENTIAL) {
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
        const manager = this.props.flowManager.credentialManager;
        const title = t('creation.bitbucket.connect');
        const errorMessage = this._getErrorMessage(manager.stateId);

        const disabled = manager.stateId === BbCredentialState.SAVE_SUCCESS;

        let result = null;

        if (manager.pendingValidation) {
            result = 'running';
        } else if (manager.stateId === BbCredentialState.SAVE_SUCCESS) {
            result = 'success';
        }

        const status = {
            result,
        };

        return (
            <FlowStep {...this.props} className="bitbucket-credentials-step" disabled={disabled} title={title}>
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
            </FlowStep>
        );
    }
}

BbCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
};

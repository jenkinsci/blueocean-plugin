import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';

import { Dialog, FormElement, PasswordInput, RadioButtonGroup, TextArea, TextInput } from '@jenkins-cd/design-language';

let t = null;

const NEW_CREDENTIAL_TYPE = {
    SSH_KEY: 'SSH_KEY',
    USER_PASS: 'USER_PASS',
    values: () => [NEW_CREDENTIAL_TYPE.SSH_KEY, NEW_CREDENTIAL_TYPE.USER_PASS],
    toLabel(option) {
        switch (option) {
            case NEW_CREDENTIAL_TYPE.SSH_KEY:
                return t('creation.git.create_credential.credential_type_ssh_key');
            case NEW_CREDENTIAL_TYPE.USER_PASS:
                return t('creation.git.create_credential.credential_type_user_pass');
            default:
                return '';
        }
    },
};

@observer
export class CreateCredentialDialog extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            newCredentialType: NEW_CREDENTIAL_TYPE.SSH_KEY, // the default
            creationPending: false,
            creationErrorMsg: null,
            sshKeyValue: null,
            sshKeyErrorMsg: null,
            usernameValue: null,
            usernameErrorMsg: null,
            passwordValue: null,
            passwordErrorMsg: null,
        };

        t = props.flowManager.translate;
    }

    _newCredentialTypeChange(option) {
        this.setState({
            newCredentialType: option,
            // if we change the credential type, reset all the values in the child forms
            sshKeyValue: null,
            usernameValue: null,
            passwordValue: null,
        });

        this._updateSSHKeyErrorMsg(true);
        this._updateUsernameErrorMsg(true);
        this._updatePasswordErrorMsg(true);
    }

    _sshKeyChange(value) {
        this.setState({
            sshKeyValue: value,
        });

        this._updateSSHKeyErrorMsg();
    }

    _updateSSHKeyErrorMsg = debounce(reset => {
        if (reset || (this.state.sshKeyErrorMsg && this.state.sshKeyValue)) {
            this.setState({
                sshKeyErrorMsg: null,
            });
        }
    }, 200);

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

    _onCreateClick() {
        const valid = this._performValidation();

        if (!valid) {
            return;
        }

        const manager = this.props.flowManager.credentialsManager;
        let promise = null;

        if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.SSH_KEY) {
            promise = manager.saveSSHKeyCredential(this.state.sshKeyValue);
        } else if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.USER_PASS) {
            promise = manager.saveUsernamePasswordCredential(this.state.usernameValue, this.state.passwordValue);
        }

        if (promise) {
            this.setState({
                creationPending: true,
            });

            promise.then(cred => this._onCreateCredentialSuccess(cred), error => this._onCreateCredentialFailure(error));
        }
    }

    _performValidation() {
        let result = true;

        if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.USER_PASS) {
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
        } else if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.SSH_KEY) {
            if (!this.state.sshKeyValue) {
                this.setState({
                    sshKeyErrorMsg: t('creation.git.create_credential.sshkey_error'),
                });

                result = false;
            }
        }

        return result;
    }

    _onCreateCredentialSuccess(credential) {
        this.setState({
            creationErrorMsg: null,
            creationPending: false,
        });

        this._onCloseClick(credential);
    }

    _onCreateCredentialFailure() {
        this.setState({
            creationErrorMsg: t('creation.git.create_credential.error_msg'),
            creationPending: false,
        });
    }

    _onCloseClick(credential) {
        if (this.props.onClose) {
            this.props.onClose(credential);
        }
    }

    render() {
        const disabled = this.state.creationPending;

        const buttons = [
            <button className="button-create-credental" disabled={disabled} onClick={() => this._onCreateClick()}>
                {t('creation.git.create_credential.button_create')}
            </button>,
            <button className="btn-secondary" disabled={disabled} onClick={() => this._onCloseClick()}>
                {t('creation.git.create_credential.button_close')}
            </button>,
        ];

        return (
            <Dialog className="create-credential-dialog" title={t('creation.git.create_credential.title')} buttons={buttons}>
                <FormElement
                    className="credentials-new"
                    title={t('creation.git.create_credential.credential_type')}
                    errorMessage={this.state.creationErrorMessage}
                    showDivider
                    verticalLayout
                >
                    <RadioButtonGroup
                        className="credentials-type-picker"
                        options={NEW_CREDENTIAL_TYPE.values()}
                        defaultOption={this.state.newCredentialType}
                        labelFunction={NEW_CREDENTIAL_TYPE.toLabel}
                        onChange={option => this._newCredentialTypeChange(option)}
                    />

                    {this.state.newCredentialType === NEW_CREDENTIAL_TYPE.SSH_KEY && (
                        <FormElement title={t('creation.git.create_credential.sshkey_title')} errorMessage={this.state.sshKeyErrorMsg}>
                            <TextArea onChange={val => this._sshKeyChange(val)} />
                        </FormElement>
                    )}

                    {this.state.newCredentialType === NEW_CREDENTIAL_TYPE.USER_PASS && (
                        <FormElement title={t('creation.git.create_credential.username_title')} errorMessage={this.state.usernameErrorMsg}>
                            <TextInput className="text-username" onChange={val => this._usernameChange(val)} />
                        </FormElement>
                    )}

                    {this.state.newCredentialType === NEW_CREDENTIAL_TYPE.USER_PASS && (
                        <FormElement title={t('creation.git.create_credential.password_title')} errorMessage={this.state.passwordErrorMsg}>
                            <PasswordInput className="text-password" onChange={val => this._passwordChange(val)} />
                        </FormElement>
                    )}
                </FormElement>
            </Dialog>
        );
    }
}

CreateCredentialDialog.propTypes = {
    flowManager: PropTypes.object,
    onClose: PropTypes.func,
};

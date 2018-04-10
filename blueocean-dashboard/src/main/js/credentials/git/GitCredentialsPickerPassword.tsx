import React, {  Component } from 'react';
import {  i18nTranslator } from '@jenkins-cd/blueocean-core-js';
import {GitPWCredentialsManager} from './GitPWCredentialsManager';

import {
    FormElement,
    PasswordInput,
    TextInput,
    Button
} from '@jenkins-cd/design-language';

import debounce from 'lodash.debounce';

import {BbCredentialsState} from '../bitbucket/BbCredentialsState';



const t = i18nTranslator('blueocean-dashboard');

interface Props {
    onStatus?: Function,
    onComplete?: Function,
    scmId: string,
    apiUrl?: string,
}

interface State {
    loading: boolean,
    usernameValue: string | null,
    usernameErrorMsg: string | null,
    passwordValue: string | null,
    passwordErrorMsg: string | null,
}

// TODO: Quick descriptive doc
export class GitCredentialsPickerPassword extends Component<Props, State> {

    credentialsManager: GitPWCredentialsManager;

    constructor(props) {
        super(props);

        console.log('debounce is', debounce);

        this.credentialsManager = new GitPWCredentialsManager();

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
        this.credentialsManager.findExistingCredential().then(credential => this._findExistingCredentialComplete(credential));
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
        this.credentialsManager
            .createCredential(this.state.usernameValue, this.state.passwordValue)
            .then(credential => this._onCreateCredentialSuccess(credential));
    }

    _onCreateCredentialSuccess(credential) {
        if (credential && this.props.onComplete) {
            this.props.onComplete(credential, 'userSelected');
        }
    }

    _getErrorMessage(stateId) {
        // TODO: Lookup / create replacements for these error label resources
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

        this._updateUsernameErrorMsg(false);
    }

    // TODO: _updateUsernameErrorMsg = debounce(reset => {
    _updateUsernameErrorMsg = (reset => {
        if (reset || (this.state.usernameErrorMsg && this.state.usernameValue)) {
            this.setState({
                usernameErrorMsg: null,
            });
        }
    });
    // TODO: }, 200);

    _passwordChange(value) {
        this.setState({
            passwordValue: value,
        });

        this._updatePasswordErrorMsg(false);
    }

    // TODO: _updatePasswordErrorMsg = debounce(reset => {
    _updatePasswordErrorMsg = (reset => {
        if (reset || (this.state.passwordErrorMsg && this.state.passwordValue)) {
            this.setState({
                passwordErrorMsg: null,
            });
        }
    });
    // TODO: }, 200);

    render() {
        const errorMessage = this._getErrorMessage(this.credentialsManager.stateId);

        let result:string|null = null;

        if (this.credentialsManager.pendingValidation) {
            result = 'running';
        } else if (this.credentialsManager.stateId === BbCredentialsState.SAVE_SUCCESS) {
            result = 'success';
        }

        const status = {
            result,
        };

        console.log('FormElement',    !!FormElement); // TODO: RM
        console.log('PasswordInput',    !!PasswordInput); // TODO: RM
        console.log('TextInput',    !!TextInput); // TODO: RM
        console.log('Button',     !!Button); // TODO: RM


        if (FormElement && PasswordInput && TextInput && Button) {
            return (
                !this.state.loading && (
                    <div className="credentials-picker-bitbucket">
                        <p className="instructions">{t('creation.bitbucket.connect.authorize')}. &nbsp;</p>
                        <FormElement className="credentials-new" errorMessage={errorMessage} verticalLayout>
                            <FormElement title={t('creation.git.create_credential.username_title')} errorMessage={this.state.usernameErrorMsg}>
                                <TextInput className="text-username" onChange={val => this._usernameChange(val)} />
                            </FormElement>
                            <FormElement title={t('creation.git.create_credential.password_title')} errorMessage={this.state.passwordErrorMsg}>
                                <PasswordInput className="text-password" onChange={val => this._passwordChange(val)} />
                            </FormElement>
                        </FormElement>
                        <Button className="button-create-credental" status={status} onClick={() => this._createCredential()}>
                            Connect
                        </Button>
                    </div>
                )
            );
        }

        return <h1>WTAF</h1>;
    }
}

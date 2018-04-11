import * as React from 'react';
import {Component} from 'react';
import {i18nTranslator} from '@jenkins-cd/blueocean-core-js';
import {GitPWCredentialsManager} from './GitPWCredentialsManager';
import * as debounce from 'lodash.debounce';
// TODO: Do we actually need debounce?

import {Button} from '../../creation/github/Button';

import {
    FormElement,
    PasswordInput,
    TextInput,
} from '@jenkins-cd/design-language';

import {BbCredentialsState} from '../bitbucket/BbCredentialsState';


const t = i18nTranslator('blueocean-dashboard');

interface Props {
    onStatus?: Function,
    onComplete?: Function,
    repositoryUrl: string,
    branch?: string,
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
        const {repositoryUrl, branch} = this.props;
        this.credentialsManager.configure(repositoryUrl, branch);
        this.credentialsManager.findExistingCredential().then(credential => this._findExistingCredentialComplete(credential));
    }

    _findExistingCredentialComplete(credential) {
        // TODO: Inline this

        console.log('GitCredentialsPickerPassword._findExistingCredentialComplete', JSON.stringify(credential, null, 4)); // TODO: RM


        this.setState({
            loading: false,
        });

        if (credential && this.props.onComplete) {
            this.props.onComplete(credential, 'autoSelected');
            // TODO: set state to "credential found"
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
        console.log('GitCredentialsPickerPassword._onCreateCredentialSuccess', JSON.stringify(credential, null, 4));// TODO: RM
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

        this._updatePasswordErrorMsg(false);
    }

    _updatePasswordErrorMsg = debounce(reset => {
        if (reset || (this.state.passwordErrorMsg && this.state.passwordValue)) {
            this.setState({
                passwordErrorMsg: null,
            });
        }
    }, 200);

    render() {

        if (this.state.loading) {
            return null;
        }

        const errorMessage = this._getErrorMessage(this.credentialsManager.stateId);

        let result: string | null = null;

        if (this.credentialsManager.pendingValidation) {
            result = 'running';
        } else if (this.credentialsManager.stateId === BbCredentialsState.SAVE_SUCCESS) {
            result = 'success';
        }

        const status = {
            result,
        };

        // TODO: Find all the messages, extract them for git-bundle
        // TODO: needs padding below connect button

        // TODO: "use selected / add new / proceed without credentials" functionality

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
}

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
    RadioButtonGroup,
} from '@jenkins-cd/design-language';

import {BbCredentialsState} from '../bitbucket/BbCredentialsState';


const t = i18nTranslator('blueocean-dashboard');

type Credential = any; // FIXME: canonical types in core-js

interface Props {
    onStatus?: Function,
    onComplete?: Function,
    repositoryUrl: string,
    branch?: string,
    existingFailed?: boolean, // if true we shouldn't bother looking it up
    requirePush?: boolean,
}

interface State {
    loading: boolean,
    usernameValue: string | null,
    usernameErrorMsg: string | null,
    passwordValue: string | null,
    passwordErrorMsg: string | null,
    existingCredential?: Credential,
    selectedRadio: RadioOption
}

enum RadioOption {
    USE_EXISTING = 'useExisting',
    CREATE_NEW = 'createNew',
}

const radioOptions = Object.values(RadioOption);


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
            selectedRadio: RadioOption.USE_EXISTING,
        };
    }

    componentWillMount() {

        this.setState(state => ({
            loading: true,
        }));

        if (this.props.onStatus) {
            this.props.onStatus('promptLoading');
        }
    }

    componentDidMount() {
        const {repositoryUrl, branch, existingFailed, onStatus} = this.props;
        const credentialsManager = this.credentialsManager;
        credentialsManager.configure(repositoryUrl, branch);

        if (existingFailed) {
            console.log('GitCredentialsPickerPassword no need to load existing'); // TODO: RM
            this.setState(state=> ({
                loading:false,
                existingCredential: undefined
            }));

            if (onStatus) {
                onStatus('promptReady');
            }

        } else {
            credentialsManager.findExistingCredential()
                .then(credential => this._findExistingCredentialComplete(credential));
        }
    }

    _findExistingCredentialComplete(credential: Credential | null) {
        // TODO: Inline this

        console.log('GitCredentialsPickerPassword._findExistingCredentialComplete', credential && credential.id); // TODO: RM

        const newState: any = {
            loading: false,
            existingCredential: credential,
        };

        if (credential && this.props.onComplete) {
            newState.selectedRadio = RadioOption.USE_EXISTING;

            if (this.props.onComplete) {
                this.props.onComplete(credential, 'autoSelected');
            }

        } else if (this.props.onStatus) {
            this.props.onStatus('promptReady');
        }

        this.setState(newState);
    }

    _createCredential() {
        const valid = this._performValidation();
        if (!valid) {
            return;
        }

        this.setState({
            existingCredential: undefined
        });
        this.credentialsManager
            .createCredential(this.state.usernameValue, this.state.passwordValue, !!this.props.requirePush)
            .then(credential => this._onCreateCredentialSuccess(credential));
    }

    _onCreateCredentialSuccess(credential) {
        console.log('GitCredentialsPickerPassword._onCreateCredentialSuccess', credential);// TODO: RM

        this.setState({
            existingCredential: credential,
            selectedRadio: RadioOption.USE_EXISTING
        });

        if (this.props.onComplete) {
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

    _radioLabel = (option) => {
        // TODO: Message resources

        const {existingCredential} = this.state;
        const displayName = existingCredential && existingCredential.displayName || '';

        switch (option) {
            case RadioOption.CREATE_NEW:
                return t('creation.git.create_credential.option_create_new');
            case RadioOption.USE_EXISTING:
                return t('creation.git.create_credential.option_existing', [displayName]);
        }
        return "";
    };

    _radioChanged = (selectedRadio: RadioOption) => {
        const {onComplete} = this.props;

        if (onComplete) {
            onComplete(selectedRadio === RadioOption.USE_EXISTING ? this.state.existingCredential : undefined);
        }

        this.setState({selectedRadio});
    };

    render() {
        console.log('GitCredentialsPickerPassword.render', this.state.loading); // TODO: RM
        if (this.state.loading) {
            return null;
        }

        console.log('GitCredentialsPickerPassword.render AAA'); // TODO: RM


        const managerState = this.credentialsManager.stateId;
        const errorMessage = this._getErrorMessage(managerState);
        const isPendingValidation = this.credentialsManager.pendingValidation;

        const connectButtonStatus = {result: null as string | null};
        console.log('GitCredentialsPickerPassword.render BBB'); // TODO: RM

        if (isPendingValidation) {
            connectButtonStatus.result = 'running';
        } else if (managerState === BbCredentialsState.SAVE_SUCCESS) {
            connectButtonStatus.result = 'success';
        }

        const {existingCredential, selectedRadio} = this.state;

        const hasExistingCredential = !!existingCredential;
        const useExistingCredential = hasExistingCredential && selectedRadio === RadioOption.USE_EXISTING;
        const disableForm = isPendingValidation || useExistingCredential;

        // TODO: Find all the messages, extract them for git-bundle
        // TODO: needs padding below connect button

        const labelInstructions = t('creation.git.create_credential.pw_instructions');
        const labelUsername = t('creation.git.create_credential.username_title');
        const labelPassword = t('creation.git.create_credential.password_title');
        const labelButton = t('creation.git.create_credential.button_create'); // TODO: resource

        console.log('GitCredentialsPickerPassword.render CCC'); // TODO: RM
        return (
            <div className="credentials-picker-bitbucket">
                <p className="instructions">{labelInstructions}</p>
                {hasExistingCredential && (
                    <RadioButtonGroup options={radioOptions}
                                      labelFunction={this._radioLabel}
                                      defaultOption={RadioOption.USE_EXISTING}
                                      onChange={this._radioChanged} />
                )}
                <FormElement className="credentials-new" errorMessage={errorMessage} verticalLayout>
                    <FormElement title={labelUsername} errorMessage={this.state.usernameErrorMsg}>
                        <TextInput disabled={disableForm} className="text-username" onChange={val => this._usernameChange(val)} />
                    </FormElement>
                    <FormElement title={labelPassword} errorMessage={this.state.passwordErrorMsg}>
                        <PasswordInput disabled={disableForm} className="text-password" onChange={val => this._passwordChange(val)} />
                    </FormElement>
                </FormElement>
                <Button disabled={disableForm} className="button-create-credental" status={connectButtonStatus} onClick={() => this._createCredential()}>
                    {labelButton}
                </Button>
            </div>
        );
    }
}

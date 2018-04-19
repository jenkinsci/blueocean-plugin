import * as React from 'react';
import {Component} from 'react';
import {i18nTranslator} from '@jenkins-cd/blueocean-core-js';
import {GitPWCredentialsManager, ManagerState} from './GitPWCredentialsManager';
import * as debounce from 'lodash.debounce';

import {Button} from '../../creation/github/Button';

import {
    FormElement,
    PasswordInput,
    TextInput,
    RadioButtonGroup,
} from '@jenkins-cd/design-language';

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

function getErrorMessage(state: ManagerState) {
    if (state === ManagerState.INVALID_CREDENTIAL) {
        return t('creation.git.create_credential.invalid_username_password');
    } else if (state === ManagerState.REVOKED_CREDENTIAL) {
        return t('creation.git.create_credential.revoked_credential');
    } else if (state === ManagerState.UNEXPECTED_ERROR_CREDENTIAL) {
        return t('creation.git.create_credential.unexpected_error');
    }
    return null;
}

/**
 * Component to handle lookup / creation of username+password credentials for git repositories over http(s)
 */
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
            this.setState(state=> ({
                loading:false,
                existingCredential: undefined
            }));

            if (onStatus) {
                onStatus('promptReady');
            }

        } else {
            credentialsManager.findExistingCredential()
                .then(credential => {
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
                });
        }
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
            .then(credential => {
                this.setState({
                    existingCredential: credential,
                    selectedRadio: RadioOption.USE_EXISTING
                });

                if (this.props.onComplete) {
                    this.props.onComplete(credential, 'userSelected');
                }
            });
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
        if (this.state.loading) {
            return null;
        }

        const managerState: ManagerState = this.credentialsManager.state;
        const errorMessage = getErrorMessage(managerState);
        const isPendingValidation = managerState == ManagerState.PENDING_VALIDATION;

        const connectButtonStatus = {result: null as string | null};

        if (isPendingValidation) {
            connectButtonStatus.result = 'running';
        } else if (managerState === ManagerState.SAVE_SUCCESS) {
            connectButtonStatus.result = 'success';
        }

        const {existingCredential, selectedRadio} = this.state;

        const hasExistingCredential = !!existingCredential;
        const useExistingCredential = hasExistingCredential && selectedRadio === RadioOption.USE_EXISTING;
        const disableForm = isPendingValidation || useExistingCredential;

        const labelInstructions = t('creation.git.create_credential.pw_instructions');
        const labelUsername = t('creation.git.create_credential.username_title');
        const labelPassword = t('creation.git.create_credential.password_title');
        const labelButton = t('creation.git.create_credential.button_create');

        return (
            <div className="credentials-picker-git">
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

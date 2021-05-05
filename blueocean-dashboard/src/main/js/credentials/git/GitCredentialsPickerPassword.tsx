import * as React from 'react';
import { observer } from 'mobx-react';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
import { GitPWCredentialsManager, ManagerState, Credential } from './GitPWCredentialsManager';
import * as debounce from 'lodash.debounce';

import { Button } from '../../creation/github/Button';

import { FormElement, PasswordInput, TextInput, RadioButtonGroup } from '@jenkins-cd/design-language';

const t = i18nTranslator('blueocean-dashboard');

interface Props {
    onStatus?: (status: string) => void;
    onComplete?: (selectedCredential: Credential | undefined, cause?: string) => void;
    repositoryUrl: string;
    branch?: string;
    existingFailed?: boolean; // if true we shouldn't bother looking it up
    requirePush?: boolean;
}

interface State {
    usernameValue: string | null;
    usernameErrorMsg: string | null;
    passwordValue: string | null;
    passwordErrorMsg: string | null;
    selectedRadio: RadioOption;
}

enum RadioOption {
    USE_EXISTING = 'useExisting',
    CREATE_NEW = 'createNew',
}

let radioOptions: string[] = [];
Object.keys(RadioOption).forEach(function(key) {
    radioOptions.push(RadioOption[key]);
});

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
@observer
export class GitCredentialsPickerPassword extends React.Component<Props, State> {
    credentialsManager: GitPWCredentialsManager;

    constructor(props) {
        super(props);

        this.credentialsManager = new GitPWCredentialsManager();

        this.state = {
            usernameValue: null,
            usernameErrorMsg: null,
            passwordValue: null,
            passwordErrorMsg: null,
            selectedRadio: RadioOption.USE_EXISTING,
        };

        const { repositoryUrl, branch, existingFailed = false } = this.props;
        this._repositoryChanged(repositoryUrl, branch, existingFailed);
    }

    componentWillMount() {
        if (this.props.onStatus) {
            this.props.onStatus('promptLoading');
        }
    }

    componentWillReceiveProps(nextProps: Props) {
        const { repositoryUrl, branch, existingFailed } = nextProps;
        if (branch !== this.props.branch || repositoryUrl !== this.props.repositoryUrl || existingFailed !== this.props.existingFailed) {
            this._repositoryChanged(repositoryUrl, branch, !!existingFailed);
        }
    }

    componentDidMount() {
        const { existingFailed, onStatus } = this.props;
        if (existingFailed) {
            if (onStatus) {
                onStatus('promptReady');
            }
        }
    }

    _repositoryChanged = debounce((repositoryUrl: string, branch: string | undefined, existingFailed: boolean) => {
        const { onStatus, onComplete } = this.props;
        const credentialsManager = this.credentialsManager;

        credentialsManager.configure(repositoryUrl, branch);

        if (!existingFailed) {
            credentialsManager.findExistingCredential().then(credential => {
                if (credential && onComplete) {
                    this.setState({ selectedRadio: RadioOption.USE_EXISTING });

                    if (onComplete) {
                        onComplete(credential, 'autoSelected');
                    }
                } else if (onStatus) {
                    onStatus('promptReady');
                }
            });
        }
    }, 500);

    _createCredential() {
        const valid = this._performValidation();
        if (!valid) {
            return;
        }

        const { usernameValue, passwordValue } = this.state;
        const { requirePush = false } = this.props;

        this.credentialsManager
            .createCredential(usernameValue, passwordValue, requirePush)
            .catch(error => {
                return undefined; // Error details handled by manager state
            })
            .then(credential => {
                this.setState({
                    selectedRadio: RadioOption.USE_EXISTING,
                    usernameValue: null,
                    passwordValue: null,
                });

                if (this.props.onComplete) {
                    // Notify even if credential undefined, so owner knows "unselected"
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

    _radioLabel = option => {
        const { existingCredential } = this.credentialsManager;
        const displayName = (existingCredential && existingCredential.displayName) || '';

        switch (option) {
            case RadioOption.CREATE_NEW:
                return t('creation.git.create_credential.option_create_new');
            case RadioOption.USE_EXISTING:
                return t('creation.git.create_credential.option_existing', [displayName]);
        }
        return '';
    };

    _radioChanged = (selectedRadio: RadioOption) => {
        const { onComplete } = this.props;

        if (onComplete) {
            onComplete(selectedRadio === RadioOption.USE_EXISTING ? this.credentialsManager.existingCredential : undefined);
        }

        this.setState({ selectedRadio });
    };

    render() {
        const managerState: ManagerState = this.credentialsManager.state;

        if (managerState === ManagerState.PENDING_LOADING_CREDS) {
            return null;
        }

        const errorMessage = getErrorMessage(managerState);
        const isPendingValidation = managerState == ManagerState.PENDING_VALIDATION;

        const connectButtonStatus = { result: null as string | null };
        const { selectedRadio } = this.state;

        const { existingCredential } = this.credentialsManager;

        const hasExistingCredential = !!existingCredential;
        const useExistingCredential = hasExistingCredential && selectedRadio === RadioOption.USE_EXISTING;
        const disableForm = isPendingValidation || useExistingCredential;

        if (isPendingValidation) {
            connectButtonStatus.result = 'running';
        } else if (managerState === ManagerState.SAVE_SUCCESS && useExistingCredential) {
            connectButtonStatus.result = 'success';
        }

        const labelInstructions = t('creation.git.create_credential.pw_instructions');
        const labelUsername = t('creation.git.create_credential.username_title');
        const labelPassword = t('creation.git.create_credential.password_title');
        const labelButton = t('creation.git.create_credential.button_create');

        return (
            <div className="credentials-picker-git">
                <p className="instructions">{labelInstructions}</p>
                {hasExistingCredential && (
                    <RadioButtonGroup
                        options={radioOptions}
                        labelFunction={this._radioLabel}
                        defaultOption={RadioOption.USE_EXISTING}
                        onChange={this._radioChanged}
                    />
                )}
                <FormElement className="credentials-new" errorMessage={errorMessage} verticalLayout>
                    <FormElement title={labelUsername} errorMessage={this.state.usernameErrorMsg}>
                        <TextInput disabled={disableForm} className="text-username" onChange={val => this._usernameChange(val)} />
                    </FormElement>
                    <FormElement title={labelPassword} errorMessage={this.state.passwordErrorMsg}>
                        <PasswordInput disabled={disableForm} className="text-password" onChange={val => this._passwordChange(val)} />
                    </FormElement>
                </FormElement>
                <Button disabled={disableForm} className="button-create-credential" status={connectButtonStatus} onClick={() => this._createCredential()}>
                    {labelButton}
                </Button>
            </div>
        );
    }
}

import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { Dropdown, FormElement, PasswordInput, RadioButtonGroup, TextArea, TextInput } from '@jenkins-cd/design-language';

import ValidationUtils from '../../util/ValidationUtils';
import debounce from 'lodash.debounce';
import pause from '../flow2/pause';

import FlowStep from '../flow2/FlowStep';

let t = null;

const NEW_CREDENTIAL_TYPE = {
    SSH_KEY: 'SSH_KEY',
    SYSTEM_SSH: 'SYSTEM_SSH',
    USER_PASS: 'USER_PASS',
    values: () => [
        NEW_CREDENTIAL_TYPE.SSH_KEY,
        NEW_CREDENTIAL_TYPE.USER_PASS,
        NEW_CREDENTIAL_TYPE.SYSTEM_SSH,
    ],
    toLabel(option) {
        switch (option) {
        case NEW_CREDENTIAL_TYPE.SSH_KEY:
            return t('creation.git.step1.credential_type_ssh_key');
        case NEW_CREDENTIAL_TYPE.SYSTEM_SSH:
            return t('creation.git.step1.credential_type_system_ssh');
        case NEW_CREDENTIAL_TYPE.USER_PASS:
            return t('creation.git.step1.credential_type_user_pass');
        default:
            return '';
        }
    },
};


/**
 * Component that accepts repository URL and credentials to initiate
 * creation of a new pipeline.
 */
@observer
export default class GitConnectStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            repositoryUrl: null,
            repositoryErrorMsg: null,
            credentialsErrorMsg: null,
            existingCredentials: null,
            selectedCredential: null,
            newCredentialType: null,
            sshKeyValue: null,
            sshKeyErrorMsg: null,
            usernameValue: null,
            usernameErrorMsg: null,
            passwordValue: null,
            passwordErrorMsg: null,
            createButtonDisabled: false,
            createInProgress: false,
        };

        t = this.props.flowManager.translate;
    }

    componentDidMount() {
        this.props.flowManager
            .listAllCredentials()
            .then(pause)
            .then(data => this._setExistingCredentials(data));
    }

    componentDidUpdate() {
        // this._updateCreateButton();
    }

    _setExistingCredentials(creds) {
        this.setState({
            existingCredentials: creds,
        });
    }

    _repositoryUrlChange(value) {
        this.setState({
            repositoryUrl: value,
        });

        this._updateRepositoryErrorMsg();
    }

    _updateRepositoryErrorMsg = debounce(() => {
        if (this.state.repositoryErrorMsg && ValidationUtils.validateUrl(this.state.repositoryUrl)) {
            this.setState({
                repositoryErrorMsg: null,
            });
        }
    }, 200);

    _selectedCredentialChange(cred) {
        this.setState({
            selectedCredential: cred,
        });

        this._updateCredentialsErrorMsg();
        this._updateSSHKeyErrorMsg();
        this._updateUsernameErrorMsg();
        this._updatePasswordErrorMsg();
    }

    _newCredentialTypeChange(option) {
        this.setState({
            newCredentialType: option,
            // if we change the credential type, reset all the values in the child forms
            sshKeyValue: null,
            usernameValue: null,
            passwordValue: null,
        });

        this._updateCredentialsErrorMsg();
        this._updateSSHKeyErrorMsg(true);
        this._updateUsernameErrorMsg(true);
        this._updatePasswordErrorMsg(true);
    }

    _updateCredentialsErrorMsg = debounce(() => {
        if (this.state.credentialsErrorMsg && (this.state.newCredentialType || this.state.selectedCredential)) {
            this.setState({
                credentialsErrorMsg: null,
            });
        }
    }, 200);

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

    _performValidation() {
        let result = true;

        if (!ValidationUtils.validateUrl(this.state.repositoryUrl)) {
            this.setState({
                repositoryErrorMsg: t('creation.git.step1.repo_error'),
            });

            result = false;
        }

        if (!this.state.newCredentialType && !this.state.selectedCredential) {
            this.setState({
                credentialsErrorMsg: t('creation.git.step1.credential_error'),
            });

            result = false;
        }

        if (this.state.selectedCredential) {
            return result;
        } else if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.SYSTEM_SSH) {
            return result;
        } else if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.USER_PASS) {
            if (!this.state.usernameValue) {
                this.setState({
                    usernameErrorMsg: t('creation.git.step1.username_error'),
                });

                result = false;
            }

            if (!this.state.passwordValue) {
                this.setState({
                    passwordErrorMsg: t('creation.git.step1.password_error'),
                });

                result = false;
            }

            return result;
        } else if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.SSH_KEY) {
            if (!this.state.sshKeyValue) {
                this.setState({
                    sshKeyErrorMsg: t('creation.git.step1.sshkey_error'),
                });

                result = false;
            }
        }

        return result;
    }

    _beginCreation() {
        const isValid = this._performValidation();

        if (!isValid) {
            return;
        }

        this.setState({
            createInProgress: true,
            createButtonDisabled: true,
        });

        if (this.state.selectedCredential) {
            this.props.flowManager.createPipeline(this.state.repositoryUrl, this.state.selectedCredential.id);
        } else if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.SSH_KEY) {
            this.props.flowManager.createWithSSHKeyCredential(this.state.repositoryUrl, this.state.sshKeyValue);
        } else if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.USER_PASS) {
            this.props.flowManager.createWithUsernamePasswordCredential(this.state.repositoryUrl, this.state.usernameValue, this.state.passwordValue);
        } else if (this.state.newCredentialType === NEW_CREDENTIAL_TYPE.SYSTEM_SSH) {
            this.props.flowManager.createWithSystemSSHCredential(this.state.repositoryUrl);
        }
    }

    render() {
        const disabled = !this.props.flowManager.isConnectEnabled;

        return (
            <FlowStep {...this.props} className="git-step-connect" title={t('creation.git.step1.title')} disabled={disabled}>
                <p className="instructions">
                    {t('creation.git.step1.instructions')} &nbsp;
                    <a href="https://jenkins.io/doc/book/pipeline/jenkinsfile/" target="_blank">{t('creation.git.step1.instructions_link')}</a>
                </p>

                <FormElement title={t('creation.git.step1.repo_title')} errorMessage={this.state.repositoryErrorMsg}>
                    <TextInput className="text-repository-url" onChange={val => this._repositoryUrlChange(val)} />
                </FormElement>

                <FormElement className="credentials" errorMessage={this.state.credentialsErrorMsg}>
                    <FormElement className="credentials-new" title={t('creation.git.step1.credential_new')} showDivider verticalLayout>
                        <RadioButtonGroup
                          className="credentials-type-picker"
                          options={NEW_CREDENTIAL_TYPE.values()}
                          labelFunction={NEW_CREDENTIAL_TYPE.toLabel}
                          onChange={option => this._newCredentialTypeChange(option)}
                        />

                        { this.state.newCredentialType === NEW_CREDENTIAL_TYPE.SSH_KEY &&
                        <FormElement title={t('creation.git.step1.sshkey_title')} errorMessage={this.state.sshKeyErrorMsg}>
                            <TextArea onChange={val => this._sshKeyChange(val)} />
                        </FormElement>
                        }

                        { this.state.newCredentialType === NEW_CREDENTIAL_TYPE.USER_PASS &&
                        <FormElement title={t('creation.git.step1.username_title')} errorMessage={this.state.usernameErrorMsg}>
                            <TextInput onChange={val => this._usernameChange(val)} />
                        </FormElement>
                        }

                        { this.state.newCredentialType === NEW_CREDENTIAL_TYPE.USER_PASS &&
                        <FormElement title={t('creation.git.step1.password_title')} errorMessage={this.state.passwordErrorMsg}>
                            <PasswordInput onChange={val => this._passwordChange(val)} />
                        </FormElement>
                        }
                    </FormElement>

                    <FormElement className="credentials-existing" title={t('creation.git.step1.credential_existing')} showDivider>
                    {
                        !this.state.existingCredentials &&
                        <div>{t('creation.git.step1.credential_loading_msg')}</div>
                    }
                    {
                        this.state.existingCredentials && !this.state.existingCredentials.length &&
                        <div>{t('creation.git.step1.credential_none_available')}</div>
                    }
                    {
                        this.state.existingCredentials && this.state.existingCredentials.length > 0 &&
                        <Dropdown
                          placeholder={t('creation.git.step1.credential_existing_placeholder')}
                          options={this.state.existingCredentials}
                          labelField="displayName"
                          onChange={opt => this._selectedCredentialChange(opt)}
                        />
                    }
                    </FormElement>
                </FormElement>

                <button
                  className="button-create-pipeline"
                  onClick={() => this._beginCreation()}
                  disabled={this.state.createButtonDisabled}
                >
                    {this.state.createInProgress ?
                        t('creation.git.step1.create_button_progress') :
                        t('creation.git.step1.create_button')}
                </button>

            </FlowStep>
        );
    }
}

GitConnectStep.propTypes = {
    flowManager: PropTypes.object,
};

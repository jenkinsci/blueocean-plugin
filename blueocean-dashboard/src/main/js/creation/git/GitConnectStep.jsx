/**
 * Created by cmeyers on 10/19/16.
 */
import React, { PropTypes } from 'react';
import { Dropdown } from '@jenkins-cd/design-language';

import FlowStep from '../FlowStep';

const CREDENTIAL_CHOICE = {
    SSH: 'SSH',
    SYSTEM_SSH: 'SYSTEM_SSH',
    USER_PASS: 'USER_PASS',
};

/**
 * Component that accepts repository URL and credentials to initiate
 * creation of a new pipeline.
 */
export default class GitConnectStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            repositoryUrl: null,
            existingCredentials: null,
            selectedCredential: null,
            credentialsSelection: null,
            sshKeyValue: null,
            usernameValue: null,
            passwordValue: null,
            createButtonDisabled: true,
            createInProgress: false,
        };
    }

    componentDidMount() {
        this.props.credentialsManager
            .listAllCredentials()
            .then(data => this._setExistingCredentials(data));
    }

    componentDidUpdate() {
        this._updateCreateButton();
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
    }

    _selectExistingCredential(cred) {
        this.setState({
            selectedCredential: cred,
        });
    }

    _credentialsChange(option) {
        this.setState({
            credentialsSelection: option,
        });
    }

    _sshKeyChange(value) {
        this.setState({
            sshKeyValue: value,
        });
    }

    _usernameChange(value) {
        this.setState({
            usernameValue: value,
        });
    }

    _passwordChange(value) {
        this.setState({
            passwordValue: value,
        });
    }

    /**
     * Enables/disables the create button based on the state of the form.
     * @private
     */
    _updateCreateButton() {
        let disabled = false;

        if (!this.state.repositoryUrl) {
            disabled = true;
        }

        if (!this.state.credentialsSelection) {
            disabled = disabled || true;
        } else if (this.state.credentialsSelection === CREDENTIAL_CHOICE.SSH) {
            disabled = disabled || !this.state.sshKeyValue;
        } else if (this.state.credentialsSelection === CREDENTIAL_CHOICE.USER_PASS) {
            disabled = disabled || (!this.state.usernameValue || !this.state.passwordValue);
        }

        if (this.state.createInProgress) {
            disabled = true;
        }

        // only set the new button state if it's out of sync; prevents nasty update loop
        if (this.state.createButtonDisabled !== disabled) {
            this.setState({
                createButtonDisabled: disabled,
            });
        }
    }

    _completeStep() {
        this.setState({
            createInProgress: true,
            createButtonDisabled: true,
        });

        if (this.state.credentialsSelection === CREDENTIAL_CHOICE.SSH) {
            this.props.manager.createWithSshKeyCredential(this.state.repositoryUrl, this.state.sshKeyValue);
        } else if (this.state.credentialsSelection === CREDENTIAL_CHOICE.USER_PASS) {
            this.props.manager.createWithUsernamePasswordCredential(this.state.repositoryUrl, this.state.usernameValue, this.state.passwordValue);
        } else if (this.state.credentialsSelection === CREDENTIAL_CHOICE.SYSTEM_SSH) {
            this.props.manager.createWithSystemSshCredential(this.state.repositoryUrl);
        }

        this.props.onCompleteStep();
    }

    render() {
        return (
            <FlowStep {...this.props} title="Connect to a Git repository">
                <p>Make sure you have a Jenkinsfile... yadda yadda.</p>

                <h2>Repository Url</h2>
                <input type="text" onChange={(e) => this._repositoryUrlChange(e.currentTarget.value)} />

                <h2>Credentials</h2>

                <div className="credentials-container">
                    <ul className="credentials-type-picker">
                        <li>
                            <label>
                                <input type="radio"
                                  value={CREDENTIAL_CHOICE.SSH}
                                  checked={this.state.credentialsSelection === CREDENTIAL_CHOICE.SSH}
                                  onChange={() => this._credentialsChange(CREDENTIAL_CHOICE.SSH)}
                                />
                                <span>SSH</span>
                            </label>
                        </li>
                        <li>
                            <label>
                                <input type="radio"
                                  value={CREDENTIAL_CHOICE.USER_PASS}
                                  checked={this.state.credentialsSelection === CREDENTIAL_CHOICE.USER_PASS}
                                  onChange={() => this._credentialsChange(CREDENTIAL_CHOICE.USER_PASS)}
                                />
                                <span>Username &amp; Password</span>
                            </label>
                        </li>
                        <li>
                            <label>
                                <input type="radio"
                                  value={CREDENTIAL_CHOICE.SYSTEM_SSH}
                                  checked={this.state.credentialsSelection === CREDENTIAL_CHOICE.SYSTEM_SSH}
                                  onChange={() => this._credentialsChange(CREDENTIAL_CHOICE.SYSTEM_SSH)}
                                />
                                <span>Use System SSH</span>
                            </label>
                        </li>
                    </ul>

                    <div className="credentials-separator">OR</div>
                    <div className="credentials-picker">
                        <Dropdown
                          placeholder="Choose credentials"
                          options={this.state.existingCredentials}
                          labelField="displayName"
                          onChange={opt => this._selectExistingCredential(opt)}
                        />
                    </div>
                </div>

                { this.state.credentialsSelection === CREDENTIAL_CHOICE.SSH &&
                <div>
                    <h2>SSH Key</h2>
                    <textarea onChange={(e) => this._sshKeyChange(e.currentTarget.value)} />
                </div>
                }

                { this.state.credentialsSelection === CREDENTIAL_CHOICE.USER_PASS &&
                <div>
                    <h2>Username</h2>
                    <input type="text" name="username" onChange={(e) => this._usernameChange(e.currentTarget.value)} />

                    <h2>Password</h2>
                    <input type="password" name="password" onChange={(e) => this._passwordChange(e.currentTarget.value)} />
                </div>
                }

                <button
                  onClick={() => this._completeStep()}
                  disabled={this.state.createButtonDisabled}
                >
                    {this.state.createInProgress ? 'Creating Pipeline...' : 'Create Pipeline'}
                </button>

            </FlowStep>
        );
    }
}

GitConnectStep.propTypes = {
    manager: PropTypes.object,
    credentialsManager: PropTypes.object,
    onCompleteStep: PropTypes.func,
};

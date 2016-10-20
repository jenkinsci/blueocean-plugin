/**
 * Created by cmeyers on 10/19/16.
 */
import React, { PropTypes } from 'react';
import FlowStep from '../FlowStep';
import GitApi from './GitCreationApi';

const CREDENTIAL_CHOICE = {
    SSH: 'SSH',
    SYSTEM_SSH: 'SYSTEM_SSH',
    USER_PASS: 'USER_PASS',
};

export default class ConnectStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            repositoryUrl: null,
            credentialsSelection: null,
            sshKeyValue: null,
            usernameValue: null,
            passwordValue: null,
            createButtonDisabled: true,
            createInProgress: false,
        };

        this._gitApi = new GitApi();
    }

    componentDidUpdate() {
        this._updateCreateButton();
    }

    _repositoryUrlChange(value) {
        this.setState({
            repositoryUrl: value,
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

        let promise = null;

        if (this.state.credentialsSelection === CREDENTIAL_CHOICE.SSH) {
            promise = this._gitApi.saveSshKeyCredential(this.state.sshKeyValue);
        } else if (this.state.credentialsSelection === CREDENTIAL_CHOICE.USER_PASS) {
            promise = this._gitApi.saveUsernamePasswordCredential(this.state.usernameValue, this.state.passwordValue);
        } else if (this.state.credentialsSelection === CREDENTIAL_CHOICE.SYSTEM_SSH) {
            promise = this._gitApi.useSystemSshCredential();
        }

        promise
            .then((credentialId) => {
                return this._gitApi.createPipeline(this.state.repositoryUrl, credentialId);
            })
            .then(() => {
                this.props.onCompleteFlow(this);
            });
    }

    render() {
        return (
            <FlowStep {...this.props} title="Connect to a Git repository">
                <p>Make sure you have a Jenkinsfile... yadda yadda.</p>

                <h2>Repository Url</h2>
                <input type="text" onChange={(e) => this._repositoryUrlChange(e.currentTarget.value)} />

                <h2>Credentials</h2>

                <ul>
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

ConnectStep.propTypes = {
    onCompleteFlow: PropTypes.func,
};

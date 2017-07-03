import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import debounce from 'lodash.debounce';
import { FormElement, PasswordInput, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import { BbCredentialState } from '../BbCredentialState';
import { Button } from '../../github/Button';

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
        this.props.flowManager.createCredential(this.state.usernameValue, this.state.passwordValue);
    }

    _getErrorMessage() {
        return null;
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
        const title = 'Connect to BitBucket';
        const errorMessage = this._getErrorMessage();

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
            <FlowStep {...this.props} className="github-credentials-step" disabled={disabled} title={title}>
                <p className="instructions">
                    Jenkins needs user credential to authorize itself with BitBucket. &nbsp;
                </p>

                <FormElement errorMessage={errorMessage}>
                    <TextInput className="text-username" onChange={val => this._usernameChange(val)} />
                    <PasswordInput className="text-password" onChange={val => this._passwordChange(val)} />
                    <Button className="button-connect" status={status} onClick={() => this._createCredential()}>Connect</Button>
                </FormElement>
            </FlowStep>
        );
    }
}

BbCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
};

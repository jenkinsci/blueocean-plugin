import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { FormElement, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import STATE from '../GithubCreationState';

const GITHUB_URL = 'https://help.github.com/articles/creating-an-access-token-for-command-line-use/';

// TODO: temporary until we get more structured errors
const ACCESS_TOKEN_INVALID = /Invalid accessToken/;
const ACCESS_TOKEN_ERROR_MISSING_SCOPES = /Invalid token, its missing scopes: ([a-z,:]+)/;


@observer
export default class GithubCredentialsStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            success: false,
            accessToken: '',
        };
    }

    _tokenChange(accessToken) {
        this.setState({
            accessToken,
        });
    }

    _createToken() {
        this.props.flowManager.createAccessToken(this.state.accessToken)
            .then(outcome => this._handleCreateToken(outcome));
    }

    _handleCreateToken(outcome) {
        const state = {};

        if (!outcome.success) {
            const regexp1 = new RegExp(ACCESS_TOKEN_INVALID);
            const regexp2 = new RegExp(ACCESS_TOKEN_ERROR_MISSING_SCOPES);

            if (regexp1.test(outcome.detail.message)) {
                state.tokenErrorMsg = 'Invalid access token.';
            } else {
                const matches = regexp2.exec(outcome.detail.message);

                if (matches.length) {
                    state.tokenErrorMsg = `Access token is missing required scopes: ${matches[1]}`;
                }
            }
        } else {
            state.success = true;
            state.tokenErrorMsg = null;
        }

        this.setState(state);
    }

    render() {
        const { flowManager } = this.props;

        const title = 'Connect to Github';
        const disabled = this.state.success;
        const buttonDisabled = flowManager.stateId === STATE.PENDING_VALIDATE_ACCESS_TOKEN;

        return (
            <FlowStep {...this.props} className="github-credentials-step" disabled={disabled} title={title}>
                <p className="instructions">
                    Jenkins needs an access key to authorize itself with Github. &nbsp;
                    <a href={GITHUB_URL} target="_blank">Learn how to create an access key.</a>
                </p>

                <FormElement errorMessage={this.state.tokenErrorMsg}>
                    <TextInput placeholder="123456abcdef" onChange={val => this._tokenChange(val)} />

                    <button className="button-connect" disabled={buttonDisabled} onClick={() => this._createToken()}>Connect</button>
                </FormElement>

                { this.state.success &&
                <div className="msg-success">Token saved successfully!</div>
                }
            </FlowStep>
        );
    }
}

GithubCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
};

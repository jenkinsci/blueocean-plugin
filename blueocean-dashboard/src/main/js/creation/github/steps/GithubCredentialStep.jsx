import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { FormElement, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import { GithubAccessTokenState } from '../GithubAccessTokenState';
import { Button } from '../Button';


const GITHUB_URL = 'https://github.com/settings/tokens/new?scopes=repo,read:user,user:email';


@observer
export default class GithubCredentialsStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            accessToken: '',
        };
    }

    _tokenChange(accessToken) {
        this.setState({
            accessToken,
        });
    }

    _createToken() {
        this.props.flowManager.createAccessToken(this.state.accessToken);
    }

    _getErrorMessage(stateId) {
        if (stateId === GithubAccessTokenState.EXISTING_REVOKED) {
            return 'The existing access token appears to have been deleted. Please create a new token.';
        } else if (stateId === GithubAccessTokenState.EXISTING_MISSING_SCOPES) {
            return 'The existing access token is missing the required scopes. Please create a new token.';
        } else if (stateId === GithubAccessTokenState.VALIDATION_FAILED_TOKEN) {
            return 'Invalid access token.';
        } else if (stateId === GithubAccessTokenState.VALIDATION_FAILED_SCOPES) {
            return 'Access token must have the following scopes: "repos" and "user:email"';
        }

        return null;
    }

    render() {
        const manager = this.props.flowManager.accessTokenManager;
        const title = 'Connect to Github';
        const errorMessage = this._getErrorMessage(manager.stateId);

        const disabled = manager.stateId === GithubAccessTokenState.SAVE_SUCCESS;

        let result = null;

        if (manager.pendingValidation) {
            result = 'running';
        } else if (manager.stateId === GithubAccessTokenState.SAVE_SUCCESS) {
            result = 'success';
        }

        const status = {
            result,
        };

        return (
            <FlowStep {...this.props} className="github-credentials-step" disabled={disabled} title={title}>
                <p className="instructions">
                    Jenkins needs an access key to authorize itself with Github. &nbsp;
                    <a href={GITHUB_URL} target="_blank">Create an access key here.</a>
                </p>

                <FormElement errorMessage={errorMessage}>
                    <TextInput className="text-token" placeholder="Your Github access token" onChange={val => this._tokenChange(val)} />

                    <Button className="button-connect" status={status} onClick={() => this._createToken()}>Connect</Button>
                </FormElement>
            </FlowStep>
        );
    }
}

GithubCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
};

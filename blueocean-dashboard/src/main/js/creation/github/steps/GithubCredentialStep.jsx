import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { ErrorMessage, FormElement, TextInput } from '@jenkins-cd/design-language';

import FlowStep from '../../flow2/FlowStep';
import { GithubAccessTokenState } from '../GithubAccessTokenState';
import { Button } from '../Button';


const GITHUB_URL = 'https://github.com/settings/tokens/new?scopes=repo,read:user,user:email';


@observer
export default class GithubCredentialsStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            apiUrl: '',
            accessToken: '',
        };
    }

    _tokenChange(accessToken) {
        this.setState({
            accessToken,
        });
    }

    _urlChange(apiUrl) {
        this.setState({
            apiUrl,
        });
    }

    _createToken() {
        const apiUrl = this.props.enterpriseMode ? this.state.apiUrl : null;
        this.props.flowManager.createAccessToken(this.state.accessToken, apiUrl);
    }

    _getGeneralErrorMessage(stateId) {
        if (stateId === GithubAccessTokenState.VALIDATION_FAILED_UNKNOWN) {
            return 'An unknown error occurred. You may try your request again.';
        }

        return null;
    }

    _getUrlErrorMessage(stateId) {
        if (stateId === GithubAccessTokenState.VALIDATION_FAILED_API_URL) {
            return 'Invalid URL.';
        }

        return null;
    }

    _getTokenErrorMessage(stateId) {
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
        const { enterpriseMode } = this.props;

        const label = !enterpriseMode ? 'GitHub' : 'GitHub Enterprise';
        const className = 'github-credentials-step' + (enterpriseMode && ' github-enterprise' || '');
        const title = `Connect to ${label}`;
        const generalErrorMessage = this._getGeneralErrorMessage(manager.stateId);
        const urlErrorMessage = this._getUrlErrorMessage(manager.stateId);
        const tokenErrorMessage = this._getTokenErrorMessage(manager.stateId);
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
            <FlowStep {...this.props} className={className} disabled={disabled} title={title}>
                <p className="instructions">
                    Jenkins needs an access key to authorize itself with {label}. &nbsp;
                    <a href={GITHUB_URL} target="_blank">Create an access key here.</a>
                </p>

                <ErrorMessage>{generalErrorMessage}</ErrorMessage>

                { !this.props.enterpriseMode &&
                <FormElement errorMessage={tokenErrorMessage}>
                    <TextInput className="text-token" placeholder="Your GitHub access token" onChange={val => this._tokenChange(val)} />

                    <Button className="button-connect" status={status} onClick={() => this._createToken()}>Connect</Button>
                </FormElement>
                }

                { this.props.enterpriseMode && [
                    <FormElement errorMessage={urlErrorMessage}>
                        <TextInput className="text-url" placeholder="Your GitHub Enterprise URL" onChange={val => this._urlChange(val)} />
                    </FormElement>,
                    <FormElement errorMessage={tokenErrorMessage}>
                        <TextInput className="text-token" placeholder="Your GitHub access token" onChange={val => this._tokenChange(val)} />
                    </FormElement>,
                    <Button className="button-connect" status={status} onClick={() => this._createToken()}>Connect</Button>,
                ]}
            </FlowStep>
        );
    }
}

GithubCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
    enterpriseMode: PropTypes.bool,
};

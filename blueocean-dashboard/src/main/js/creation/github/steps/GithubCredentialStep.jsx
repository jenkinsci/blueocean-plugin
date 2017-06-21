import React, { PropTypes } from 'react';
import { observer } from 'mobx-react';
import { ErrorMessage, FormElement, TextInput } from '@jenkins-cd/design-language';
import debounce from 'lodash.debounce';

import FlowStep from '../../flow2/FlowStep';
import { GithubAccessTokenState } from '../GithubAccessTokenState';
import { Button } from '../Button';


function getCreateTokenUrl(apiUrl) {
    // TODO: needs testing w/ real GHE endpoint as api domain and web UI domain are likely different
    // use 'github.com' for blank or api.github.com usages
    const baseUrl = !apiUrl || apiUrl.indexOf('https://api.github.com') === 0 ?
        'https://github.com' : apiUrl;
    return `${baseUrl}/settings/tokens/new?scopes=repo,read:user,user:email`;
}


@observer
export default class GithubCredentialsStep extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            apiUrl: '',
            accessToken: '',
        };
    }

    _urlChange(apiUrl) {
        this.setState({
            apiUrl,
        });

        this._fetchExistingCredential(apiUrl);
    }

    _tokenChange(accessToken) {
        this.setState({
            accessToken,
        });
    }

    _fetchExistingCredential = debounce(apiUrl => {
        this.props.flowManager.findExistingCredential(apiUrl);
    }, 500);

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

    _getInstructions(stateId) {
        const githubUrl = getCreateTokenUrl(this.state.apiUrl);

        if (!this.props.enterpriseMode) {
            return (
                <p className="instructions">
                    Jenkins needs an access key to authorize itself with GitHub. <br />
                    <a href={githubUrl} target="_blank">Create an access key here.</a>
                </p>
            );
        }

        switch (stateId) {
        case GithubAccessTokenState.INITIAL:
        case GithubAccessTokenState.VALIDATION_FAILED_API_URL:
            return (
                <p className="instructions">
                    Jenkins needs an access key to authorize itself with GitHub. <br /> Enter the API URL below.
                </p>
            );
        case GithubAccessTokenState.EXISTING_WAS_FOUND:
            return (
                <p className="instructions">
                    An existing access token for {this.state.apiUrl} was found and will be used.
                </p>
            );
        default:
            return (
                <p className="instructions">
                    Jenkins needs an access key to authorize itself with GitHub Enterprise. <br />
                    <a href={githubUrl} target="_blank">Create an access key here.</a>
                </p>
            );
        }
    }

    render() {
        const manager = this.props.flowManager.accessTokenManager;
        const { enterpriseMode } = this.props;

        const className = 'github-credentials-step' + (enterpriseMode && ' github-enterprise' || '');
        const label = !enterpriseMode ? 'GitHub' : 'GitHub Enterprise';
        const title = `Connect to ${label}`;
        const instructions = this._getInstructions(manager.stateId);
        const generalErrorMessage = this._getGeneralErrorMessage(manager.stateId);
        const urlErrorMessage = this._getUrlErrorMessage(manager.stateId);
        const tokenErrorMessage = this._getTokenErrorMessage(manager.stateId);
        const noValidApiUrl = manager.stateId === GithubAccessTokenState.INITIAL || manager.stateId === GithubAccessTokenState.VALIDATION_FAILED_API_URL;
        const disabled = manager.stateId === GithubAccessTokenState.SAVE_SUCCESS || manager.stateId === GithubAccessTokenState.EXISTING_WAS_FOUND;

        let result = null;

        if (manager.pendingValidation) {
            result = 'running';
        } else if (manager.stateId === GithubAccessTokenState.SAVE_SUCCESS || manager.stateId === GithubAccessTokenState.EXISTING_WAS_FOUND) {
            result = 'success';
        }

        const status = {
            result,
        };

        return (
            <FlowStep {...this.props} className={className} disabled={disabled} title={title}>
                {instructions}

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
                    !noValidApiUrl &&
                    <FormElement errorMessage={tokenErrorMessage}>
                        <TextInput className="text-token" placeholder="Your GitHub access token" onChange={val => this._tokenChange(val)} />
                    </FormElement>,
                    !noValidApiUrl &&
                    <Button className="button-connect" status={status} disabled={disabled} onClick={() => this._createToken()}>Connect</Button>,
                ]}
            </FlowStep>
        );
    }
}

GithubCredentialsStep.propTypes = {
    flowManager: PropTypes.object,
    enterpriseMode: PropTypes.bool,
};

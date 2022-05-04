import { Fetch, UrlConfig, Utils, AppConfig, UrlBuilder } from '@jenkins-cd/blueocean-core-js';

import { TypedError } from '../TypedError';

import { LoadError, SaveError } from '../bitbucket/BbCredentialsApi';

export interface GitPWCredentialsApiPublic {
    findExistingCredential(repositoryUrl);
    createCredential(repositoryUrl, userName, password, branchName, requirePush);
}

/**
 * Api class to interact with GitScm class when working with username+password credentials for http(s) repos
 */
export class GitPWCredentialsApi implements GitPWCredentialsApiPublic {
    _fetch: Function;
    organization: string;

    constructor() {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
    }

    findExistingCredential(repositoryUrl) {
        const root = UrlConfig.getJenkinsRootURL();
        const credUrl = Utils.cleanSlashes(`${root}/blue/rest/organizations/${this.organization}/scm/git/?repositoryUrl=${repositoryUrl}`);

        // Create error in sync code for better stack trace
        const possibleError = new TypedError();

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        };

        return this._fetch(credUrl, {fetchOptions}).then(
            result => this._findExistingCredentialSuccess(result),
            error => {
                const { responseBody } = error;

                if (responseBody.message.indexOf('Existing credential failed') >= 0) {
                    throw possibleError.populate(LoadError.TOKEN_REVOKED, responseBody);
                }

                throw possibleError.populate(LoadError.TOKEN_INVALID, responseBody);
            }
        );
    }

    _findExistingCredentialSuccess(gitScm) {
        const credentialId = gitScm && gitScm.credentialId;

        if (!credentialId) {
            throw new TypedError(LoadError.TOKEN_NOT_FOUND);
        }

        return this._getCredential(credentialId);
    }

    _getCredential(credentialId) {
        const orgUrl = UrlBuilder.buildRestUrl(this.organization);
        const credentialUrl = `${orgUrl}credentials/user/domains/blueocean-git-domain/credentials/${encodeURIComponent(credentialId)}/`;

        return this._fetch(credentialUrl);
    }

    createCredential(repositoryUrl, userName, password, branchName, requirePush) {
        const path = UrlConfig.getJenkinsRootURL();
        const validateCredUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/git/validate`);

        const requestBody: any = {
            userName,
            password,
            repositoryUrl,
        };

        if (branchName) {
            requestBody.branch = branchName;
        }

        if (requirePush) {
            requestBody.repositoryUrl = true; // Only set if true!
        }

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        // Create error in sync code for better stack trace
        const possibleError = new TypedError();

        return this._fetch(validateCredUrl, { fetchOptions }).catch(error => {
            const { code = -1 } = error.responseBody || {};

            if (code === 401) {
                throw possibleError.populate(SaveError.INVALID_CREDENTIAL, error);
            }

            throw possibleError.populate(SaveError.UNKNOWN_ERROR, error);
        });
    }
}

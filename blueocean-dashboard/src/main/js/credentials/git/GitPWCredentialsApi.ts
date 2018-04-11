import { Fetch,
    UrlConfig,
    Utils,
    AppConfig
} from '@jenkins-cd/blueocean-core-js';

import { TypedError} from "../TypedError";

import {
    LoadError,
    SaveError,
} from '../bitbucket/BbCredentialsApi'; // TODO: move these out of BB tree?

// TODO: Docs
export class GitPWCredentialsApi {

    _fetch: any; // TODO: not any
    organization: string;

    constructor() {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
    }

    findExistingCredential(repositoryUrl) {
        // TODO: make sure this succeeeds when we already have a credential for this repourl
        const root = UrlConfig.getJenkinsRootURL();
        const credUrl = Utils.cleanSlashes(`${root}/blue/rest/organizations/${this.organization}/scm/git/?repositoryUrl=${repositoryUrl}`);
        // TODO: move Utils.cleanSlashes into UrlUtils

        // console.log('findExistingCredential', credUrl); // TODO: RM

        return this._fetch(credUrl).then(result => this._findExistingCredentialSuccess(result), error => this._findExistingCredentialFailure(error));
        // TODO: do we have to use 2-func "then" here?
        // TODO: Do we need to have success / failure as instance methods rather than inline?
    }

    _findExistingCredentialSuccess(gitScm) {
        // console.log('GitPWCredentialsApi._findExistingCredentialSuccess\n' + JSON.stringify(gitScm,null,4)); // TODO: RM
        if (gitScm && gitScm.credentialId) {
            return gitScm;
        }

        throw new TypedError(LoadError.TOKEN_NOT_FOUND);
    }

    _findExistingCredentialFailure(error) {
        // console.log('GitPWCredentialsApi._findExistingCredentialFailure', JSON.stringify(error,null,4)); // TODO: RM
        const { responseBody } = error;

        if (responseBody.message.indexOf('Existing credential failed') >= 0) {
            throw new TypedError(LoadError.TOKEN_REVOKED, responseBody);
        }

        throw new TypedError(LoadError.TOKEN_INVALID, responseBody);
    }

    createCredential(repositoryUrl, userName, password) {
        const path = UrlConfig.getJenkinsRootURL();
        const validateCredUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/git/validate`);

        const requestBody = {
            userName,
            password,
            repositoryUrl,
        };

        console.log('createCredential', validateCredUrl, JSON.stringify(requestBody,null,4)); // TODO: RM

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(validateCredUrl, { fetchOptions }).catch(error => this._createCredentialFailure(error));
    }

    _createCredentialFailure(error) {
        // TODO: inline this to create method
        // TODO: move error creation into sync code

        const { code } = error.responseBody;

        if (code === 401) {
            throw new TypedError(SaveError.INVALID_CREDENTIAL, error);
        }

        throw new TypedError(SaveError.UNKNOWN_ERROR, error);
    }
}

import { capabilityAugmenter, Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';

export class BbCredentialsApi {
    constructor(scmId, fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId;
    }

    findExistingCredential(apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        const credUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/?apiUrl=${apiUrl}`);

        return this._fetch(credUrl)
            .then(credential => capabilityAugmenter.augmentCapabilities(credential));
    }

    createBbCredential(apiUrl, userName, password) {
        const path = UrlConfig.getJenkinsRootURL();
        const validateCredUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/validate`);

        const requestBody = {
            userName,
            password,
            apiUrl,
        };

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(validateCredUrl, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(
                token => this._createCredentialSuccess(token),
                error => this.__createCredentialFailure(error)
            );
    }

    _createCredentialSuccess(credential) {
        return {
            success: true,
            credential,
        };
    }

    __createCredentialFailure(error) {
        const { message } = error.responseBody;

        return {
            success: false,
            message,
        };
    }
}

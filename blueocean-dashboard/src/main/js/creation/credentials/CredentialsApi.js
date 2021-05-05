import { capabilityAugmenter, Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';

const DOMAIN = 'blueocean-git-domain';
const SCOPE = 'USER';

function getCredentialsUrl(organization) {
    const path = UrlConfig.getRestBaseURL();
    return Utils.cleanSlashes(`${path}/organizations/${organization}/credentials/user/`);
}

export class CredentialsApi {
    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
    }

    listAllCredentials() {
        const path = UrlConfig.getRestBaseURL();
        const searchUrl = Utils.cleanSlashes(`${path}/search?q=type:credential;organization:${this.organization}`, false);

        return this._fetch(searchUrl)
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(creds => this._listAllCredentialsSuccess(creds), error => this._listAllCredentialsFailure(error));
    }

    _listAllCredentialsSuccess(credentials) {
        return {
            success: true,
            credentials,
        };
    }

    _listAllCredentialsFailure() {
        return {
            success: false,
        };
    }

    saveUsernamePasswordCredential(username, password) {
        const requestUrl = getCredentialsUrl(this.organization);

        const requestBody = {
            credentials: {
                $class: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                'stapler-class': 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                scope: SCOPE,
                domain: DOMAIN,
                username,
                password,
                description: null,
            },
        };

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(requestUrl, { fetchOptions });
    }

    saveSSHKeyCredential(privateKey) {
        const requestUrl = getCredentialsUrl(this.organization);

        const requestBody = {
            credentials: {
                $class: 'com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey',
                scope: SCOPE,
                domain: DOMAIN,
                username: null,
                passphrase: null,
                description: null,
                privateKeySource: {
                    privateKey,
                    'stapler-class': 'com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$DirectEntryPrivateKeySource',
                },
            },
        };

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(requestUrl, { fetchOptions });
    }
}

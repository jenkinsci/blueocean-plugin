import { capabilityAugmenter, Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';

export class CredentialsApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    listAllCredentials() {
        const path = UrlConfig.getJenkinsRootURL();
        const searchUrl = Utils.cleanSlashes(`${path}/blue/rest/search?q=type:credential`, false);

        return this._fetch(searchUrl)
            .then(data => capabilityAugmenter.augmentCapabilities(data));
    }

    saveUsernamePasswordCredential(username, password) {
        const path = UrlConfig.getJenkinsRootURL();
        const requestUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/credentials/system/domains/_/credentials/`);

        const requestBody = {
            credentials: {
                $class: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                'stapler-class': 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                scope: null,
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
        const path = UrlConfig.getJenkinsRootURL();
        const requestUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/credentials/system/domains/_/credentials/`);

        const requestBody = {
            credentials: {
                $class: 'com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey',
                scope: null,
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

    saveSystemSSHCredential(id, description) {
        const path = UrlConfig.getJenkinsRootURL();
        const requestUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/credentials/system/domains/_/credentials/`);

        const requestBody = {
            credentials: {
                $class: 'com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey',
                id,
                description,
                scope: null,
                username: null,
                passphrase: null,
                privateKeySource: {
                    'stapler-class': 'com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey$UsersPrivateKeySource',
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

import { action, observable } from 'mobx';
import { logging } from '@jenkins-cd/blueocean-core-js';

import waitAtLeast from '../flow2/waitAtLeast';

const SAVE_DELAY = 1000;

export class CredentialsManager {
    @observable credentials = [];

    constructor(credentialsApi) {
        this._api = credentialsApi;
    }

    listAllCredentials() {
        return this._api.listAllCredentials().then(result => {
            this._setCredentials(result.credentials);
            return this.credentials;
        });
    }

    @action
    _setCredentials(credentialList) {
        this.credentials = credentialList;
    }

    saveSSHKeyCredential(sshKey) {
        return this._api
            .saveSSHKeyCredential(sshKey)
            .then(waitAtLeast(SAVE_DELAY))
            .then(cred => this._saveSSHKeyCredentialSuccess(cred));
    }

    @action
    _saveSSHKeyCredentialSuccess(cred) {
        this.credentials.push(cred);
        return cred;
    }

    saveUsernamePasswordCredential(username, password) {
        return this._api
            .saveUsernamePasswordCredential(username, password)
            .then(waitAtLeast(SAVE_DELAY))
            .then(cred => this._saveUsernamePasswordCredentialSuccess(cred));
    }

    @action
    _saveUsernamePasswordCredentialSuccess(cred) {
        this.credentials.push(cred);
        return cred;
    }
}

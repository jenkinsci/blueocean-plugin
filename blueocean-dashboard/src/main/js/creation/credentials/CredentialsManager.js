import { action, computed, observable } from 'mobx';

import waitAtLeast from '../flow2/waitAtLeast';


const SAVE_DELAY = 1000;
// constant used to defined the special 'system ssh' key to ensure it's only created once.
const SYSTEM_SSH_ID = 'git-ssh-key-master';


export class CredentialsManager {

    @observable
    credentials = [];

    @computed
    get displayedCredentials() {
        return this._filterCredentials(this.credentials);
    }


    constructor(credentialsApi) {
        this._api = credentialsApi;
    }

    listAllCredentials() {
        return this._api.listAllCredentials()
            .then(result => this._listAllCredentialsComplete(result));
    }

    @action
    _listAllCredentialsComplete(result) {
        const creds = result.credentials || [];
        this.credentials.replace(creds);
        return result;
    }

    _filterCredentials(credentialList) {
        // 'system ssh' was removed as an option in JENKINS-42120
        // leaving in logic to filter it out in case it was created previously
        return credentialList
            .filter(item => item.id !== SYSTEM_SSH_ID);
    }

    saveSSHKeyCredential(sshKey) {
        return this._api.saveSSHKeyCredential(sshKey)
            .then(waitAtLeast(SAVE_DELAY))
            .then(cred => this._saveSSHKeyCredentialSuccess(cred));
    }

    @action
    _saveSSHKeyCredentialSuccess(cred) {
        this.credentials.push(cred);
        return cred;
    }

    saveUsernamePasswordCredential(username, password) {
        return this._api.saveUsernamePasswordCredential(username, password)
            .then(waitAtLeast(SAVE_DELAY))
            .then(cred => this._saveUsernamePasswordCredentialSuccess(cred));
    }

    @action
    _saveUsernamePasswordCredentialSuccess(cred) {
        this.credentials.push(cred);
        return cred;
    }

}

import { action, computed, observable } from 'mobx';

import waitAtLeast from '../flow2/waitAtLeast';


const SAVE_DELAY = 1000;
// constants used to defined the special 'system ssh' key to ensure it's only created once, then reused.
const SYSTEM_SSH_ID = 'git-ssh-key-master';
const SYSTEM_SSH_DESCRIPTION = 'Master SSH Key for Git Creation';


export class CredentialsManager {

    @observable
    credentials = [];

    systemSSHCredential = null;

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
        this._storeSystemSshCredential(creds);
        this.credentials.replace(creds);
        return result;
    }

    _filterCredentials(credentialList) {
        // remove 'system ssh' from the main list
        return credentialList
            .filter(item => item.id !== SYSTEM_SSH_ID);
    }

    _storeSystemSshCredential(credentialList) {
        // find the special 'system ssh' credential if it was already created
        const systemSSH = credentialList
            .filter(item => item.id === SYSTEM_SSH_ID)
            .pop();

        if (systemSSH) {
            this.systemSSHCredential = systemSSH;
        }
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

    saveSystemSSHCredential() {
        return this._api.saveSystemSSHCredential(SYSTEM_SSH_ID, SYSTEM_SSH_DESCRIPTION)
            .then(waitAtLeast(SAVE_DELAY))
            .then(cred => this._saveSystemSSHCredentialSuccess(cred));
    }

    @action
    _saveSystemSSHCredentialSuccess(cred) {
        this.credentials.push(cred);
        return cred;
    }

}

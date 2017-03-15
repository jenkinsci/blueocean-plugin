import { action, observable } from 'mobx';
import { logging } from '@jenkins-cd/blueocean-core-js';

import waitAtLeast from '../flow2/waitAtLeast';


const LOGGER = logging.logger('io.jenkins.blueocean.credentials');
const SAVE_DELAY = 1000;
// constant used to defined the special 'system ssh' key to ensure it's only created once.
const SYSTEM_SSH_ID = 'git-ssh-key-master';
const SYSTEM_SSH_DESCRIPTION = 'Master SSH Key for Git Creation';


export class CredentialsManager {

    @observable
    credentials = [];

    systemSSHCredential = null;


    constructor(credentialsApi) {
        this._api = credentialsApi;
    }

    listAllCredentials() {
        return this._api.listAllCredentials()
            .then(result => this._listAllCredentialsComplete(result));
    }

    @action
    _listAllCredentialsComplete(result) {
        const credentialList = result.credentials || [];
        this._storeCredentials(credentialList);
        return this._createSystemSSHCredentialConditionally();
    }

    _storeCredentials(credentialList) {
        // find the special 'system ssh' credential if it was already created
        const systemSSH = credentialList
            .filter(item => item.id === SYSTEM_SSH_ID)
            .pop();

        if (systemSSH) {
            LOGGER.debug(`${SYSTEM_SSH_ID} credential was found`);
            this.systemSSHCredential = systemSSH;
        }

        const filtered = credentialList
            .filter(item => item.id !== SYSTEM_SSH_ID);

        this.credentials.replace(filtered);
    }

    _createSystemSSHCredentialConditionally() {
        if (!this.systemSSHCredential) {
            LOGGER.debug(`creating ${SYSTEM_SSH_ID} credential`);
            return this.saveSystemSSHCredential()
                .then(() => this.credentials);
        }

        return this.credentials;
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
            .then(cred => this._saveSystemSSHCredentialSuccess(cred));
    }

    @action
    _saveSystemSSHCredentialSuccess(cred) {
        LOGGER.debug(`created ${SYSTEM_SSH_ID} credential successfully`);
        this.systemSSHCredential = cred;
        return cred;
    }

}

import {action, observable} from 'mobx';

/**
 * Manages retrieving, validating and saving the Perforce credentials.
 * Also holds the state of the credential for use in PerforceCredentialStep.
 */
class PerforceCredentialsManager {
    @observable credentials = [];


    constructor(credentialsApi) {
        this.credentialsApi = credentialsApi;
    }

    @action
    findExistingCredential() {
        return this.credentialsApi.findExistingCredential().then(credentials => this._onfindCredSuccess(credentials));
    }

    @action
    _onfindCredSuccess(creds) {
        // We need only perforce credentials, so filter the non Perforce credentials out
        //TODO Is there a better way of doing this?
        const length = creds.length;
        for (let i = 0; i < length; i++) {
            const obj = creds[i];
            if (obj.typeName.startsWith("Perforce")) {
                this.credentials.push(obj);
            }
        }
        return this.credentials;
    }

}

export default PerforceCredentialsManager;

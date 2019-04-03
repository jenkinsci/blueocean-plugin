import {action, observable} from 'mobx';

import PromiseDelayUtils from '../../util/PromiseDelayUtils';

const MIN_DELAY = 500;
const {delayBoth} = PromiseDelayUtils;

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
    _onfindCredSuccess(credentials) {
        // We need only perforce credentials, so filter the non Perforce credentials out
        //TODO Is there a better way of doing this?
        const length = credentials.credentials.length;
        for (let i = 0; i < length; i++) {
            const obj = credentials.credentials[i];
            if (obj.typeName.startsWith("Perforce")) {
                this.credentials.push(obj);
                console.log(this.credentials.length);
            }
        }
        return credentials;
    }

}

export default PerforceCredentialsManager;

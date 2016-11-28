/**
 * Created by cmeyers on 11/22/16.
 */

export class CredentialsManager {

    constructor(credentialsApi) {
        this._api = credentialsApi;
    }

    listAllCredentials() {
        return this._api.listAllCredentials();
    }

}

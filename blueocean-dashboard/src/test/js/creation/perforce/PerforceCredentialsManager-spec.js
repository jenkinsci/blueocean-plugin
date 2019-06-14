import {mockExtensionsForI18n} from '../../mock-extensions-i18n';
import PerforceCredentialsManager from '../../../../main/js/credentials/perforce/PerforceCredentialsManager';
import {TypedError} from "../../../../main/js/credentials/TypedError";
import {LoadError} from "../../../../main/js/credentials/perforce/PerforceCredentialsApi";
import Promise from "bluebird";

mockExtensionsForI18n();

describe('PerforceCredentialsManager', () => {
    let manager;        //instance of this class: PerforceCredentialsManager
    let credApi;        //mock of PerforceCredentialsApi

    beforeEach(() => {
        credApi = new PerforceCredentialsApiMock();
        manager = new PerforceCredentialsManager(credApi);
    });

    describe('findExistingCredential', () => {
        it('behaves when found', () => {
            expect.assertions(4);
            credApi.findExistingCredentialShouldSucceed = true;
            credApi.credentials = [
                {
                    "id": "p4poke",
                    "typeName": "Perforce Password Credential"
                },
                {
                    "id": "otherNonPerforce",
                    "typeName": "Password Credential"
                },
            ];

            return manager.findExistingCredential()
                .then(creds => {
                    expect(creds).toBeDefined();
                    expect(creds[0].id).toBe("p4poke");
                    expect(creds[0].typeName).toBe("Perforce Password Credential");
                    expect(creds[1]).toBeUndefined();   //PerforceCredentialsManager is expected to filter non-perforce credentials
                });
        });

    });
});

// Helpers

function later(promiseResolver) {
    return new Promise((resolve, reject) => {
        process.nextTick(() => {
            try {
                resolve(promiseResolver());
            }
            catch (err) {
                reject(err);
            }
        });
    });
}

class PerforceCredentialsApiMock {

    findExistingCredentialShouldSucceed = false;
    credentials = [];

    findExistingCredential() {
        return later(() => {
            if (this.findExistingCredentialShouldSucceed) {
                return this.credentials;
            }
            throw new TypedError(LoadError.TOKEN_NOT_FOUND);
        });
    }

}

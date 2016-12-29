import es6Promise from 'es6-promise'; es6Promise.polyfill();
import { capabilityAugmenter, Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import TempUtils from '../TempUtils';

export class CredentialsApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    listAllCredentials() {
        const path = UrlConfig.getJenkinsRootURL();
        const searchUrl = TempUtils.cleanSlashes(`${path}/blue/rest/search?q=type:credential`, false);

        return this._fetch(searchUrl)
            .then(data => capabilityAugmenter.augmentCapabilities(data));
    }

    // eslint-disable-next-line no-unused-vars
    saveSshKeyCredential(key) {
        const credentialId = Math.random() * Number.MAX_SAFE_INTEGER;
        const promise = new Promise(resolve => {
            setTimeout(() => {
                resolve({
                    credentialId,
                });
            }, 2000);
        });

        return promise;
    }

    // eslint-disable-next-line no-unused-vars
    saveUsernamePasswordCredential(username, password) {
        return this.saveSshKeyCredential();
    }

    useSystemSshCredential() {
        return this.saveSshKeyCredential();
    }

}

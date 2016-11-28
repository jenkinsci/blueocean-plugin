/**
 * Created by cmeyers on 11/22/16.
 */
import { capabilityAugmenter, Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';

export class CredentialsApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    listAllCredentials() {
        const path = UrlConfig.getJenkinsRootURL();
        const searchUrl = Utils.cleanSlashes(`${path}/blue/rest/search?q=type:credential`);

        return this._fetch(searchUrl)
            .then(data => capabilityAugmenter.augmentCapabilities(data));
    }

}

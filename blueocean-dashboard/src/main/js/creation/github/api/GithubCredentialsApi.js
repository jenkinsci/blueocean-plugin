import { capabilityAugmenter, Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import TempUtils from '../../TempUtils';

export class GithubCredentialsApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    findExistingCredential() {
        const path = UrlConfig.getJenkinsRootURL();
        const credUrl = TempUtils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/scm/github`);

        return this._fetch(credUrl)
            .then(credential => capabilityAugmenter.augmentCapabilities(credential));
    }

    createAccessToken(accessToken) {
        const path = UrlConfig.getJenkinsRootURL();
        const tokenUrl = TempUtils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/scm/github/validate`);

        const requestBody = {
            accessToken,
        };

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(tokenUrl, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data));
    }

}

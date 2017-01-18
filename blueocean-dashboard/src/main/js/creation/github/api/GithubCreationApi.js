import { capabilityAugmenter, Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import TempUtils from '../../TempUtils';

export class GithubCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    listOrganizations(credentialId) {
        const path = UrlConfig.getJenkinsRootURL();
        const orgsUrl = TempUtils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/scm/github/organizations/?credentialId=${credentialId}`, false);

        return this._fetch(orgsUrl)
            .then(credential => capabilityAugmenter.augmentCapabilities(credential));
    }

    listRepositories(credentialId, organizationName, pageNumber = 0, pageSize = 10) {
        const path = UrlConfig.getJenkinsRootURL();
        const reposUrl = TempUtils.cleanSlashes(
            `${path}/blue/rest/organizations/jenkins/scm/github/organizations/${organizationName}/repositories/` +
            `?credentialId=${credentialId}&pageNumber=${pageNumber}&pageSize=${pageSize}`,
            false);

        return this._fetch(reposUrl)
            .then(response => capabilityAugmenter.augmentCapabilities(response))
            .then(response => response.repositories.items);
    }

}

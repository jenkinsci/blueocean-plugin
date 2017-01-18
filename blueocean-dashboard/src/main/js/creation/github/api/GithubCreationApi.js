import { capabilityAugmenter, Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import TempUtils from '../../TempUtils';

export class GithubCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    listOrganizations(credentialId) {

    }

    listRepositories(credentialId, organization, pageNumber = 0, pageSize = 10) {

    }

}

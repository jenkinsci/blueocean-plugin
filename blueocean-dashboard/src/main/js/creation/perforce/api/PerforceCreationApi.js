import {Enum} from "../../flow2/Enum";
import {AppConfig, capabilityAugmenter, Fetch, UrlConfig, Utils} from "@jenkins-cd/blueocean-core-js";
import GithubApiUtils from "../../github/api/GithubApiUtils";

const INVALID_ACCESS_TOKEN_CODE = 428;
const INVALID_ACCESS_TOKEN_MSG = 'Invalid Github accessToken';
const INVALID_SCOPES_MSG = 'Github accessToken does not have required scopes';

export const ListOrganizationsOutcome = new Enum({
    SUCCESS: 'success',
    INVALID_TOKEN_REVOKED: 'revoked_token',
    INVALID_TOKEN_SCOPES: 'invalid_token_scopes',
    ERROR: 'error',
});


/**
 * Handles lookup of Perforce projects and repos.
 */
export default class PerforceCreationApi {
    constructor(scmId) {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        console.log("PerforceCreationApi: constructor: scmId: " + scmId);
        this.scmId = scmId || 'perforce';
        console.log("PerforceCreationApi: constructor: scmId: " + this.scmId);

    }

    /*listOrganizations(credentialId, apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        const orgsUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/organizations/?credentialId=${credentialId}`,
            false
        );
        //TODO Do this only if required
        //orgsUrl = GithubApiUtils.appendApiUrlParam(orgsUrl, apiUrl);

        return this._fetch(orgsUrl)
            .then(orgs => capabilityAugmenter.augmentCapabilities(orgs))
            .then(orgs => this._listOrganizationsSuccess(orgs), error => this._listOrganizationsFailure(error));
    }*/
}

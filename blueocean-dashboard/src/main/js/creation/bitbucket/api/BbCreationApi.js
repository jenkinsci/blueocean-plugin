import { capabilityAugmenter, Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';
import { Enum } from '../../flow2/Enum';

export const ListOrganizationsOutcome = new Enum({
    SUCCESS: 'success',
    INVALID_CREDENTIAL_ID: 'invalid_credential_id',
    ERROR: 'error',
});

export class BbCreationApi {
    constructor(scmId, fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId;
    }

    listOrganizations(credentialId, apiUrl) {
        const path = UrlConfig.getJenkinsRootURL();
        const orgsUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/organizations/?credentialId=${credentialId}&apiUrl=${apiUrl}`,
            false);

        return this._fetch(orgsUrl)
            .then(orgs => capabilityAugmenter.augmentCapabilities(orgs))
            .then(
                orgs => this._listOrganizationsSuccess(orgs),
                error => this._listOrganizationsFailure(error),
            );
    }

    _listOrganizationsSuccess(organizations) {
        return {
            outcome: 'SUCCESS',
            organizations,
        };
    }

    _listOrganizationsFailure(error) {
        const { code, message } = error.responseBody;
        if (code === 400) {
            const e = JSON.parse(message);
            if (e.field === 'credentialId') {
                return {
                    outcome: ListOrganizationsOutcome.INVALID_CREDENTIAL_ID,
                };
            }
        }
        return {
            outcome: ListOrganizationsOutcome.ERROR,
            error: message,
        };
    }

    listRepositories(credentialId, apiUrl, organizationName, pageNumber = 1, pageSize = 100) {
        const path = UrlConfig.getJenkinsRootURL();
        const reposUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/organizations/${organizationName}/repositories/` +
            `?credentialId=${credentialId}&pageNumber=${pageNumber}&pageSize=${pageSize}&apiUrl=${apiUrl}`);

        return this._fetch(reposUrl)
            .then(response => capabilityAugmenter.augmentCapabilities(response));
    }

    createMbp(credentialId, apiUrl, bbOrganization, repoName) {
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/pipelines/`);

        const requestBody = this._buildRequestBody(
            credentialId, apiUrl, `${bbOrganization.name}/${repoName}`, bbOrganization.key, repoName,
        );

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(createUrl, { fetchOptions })
            .then(pipeline => capabilityAugmenter.augmentCapabilities(pipeline));
    }

    _buildRequestBody(credentialId, apiUrl, itemName, organizationName, repoName) {
        return {
            name: itemName,
            $class: 'io.jenkins.blueocean.blueocean_bitbucket_pipeline.BitbucketPipelineCreateRequest',
            scmConfig: {
                credentialId,
                uri: apiUrl,
                config: {
                    repoOwner: organizationName,
                    repository: repoName,
                },
            },
        };
    }

}

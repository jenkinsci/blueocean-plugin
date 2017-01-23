import { capabilityAugmenter, Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';

export class GithubCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    listOrganizations(credentialId) {
        const path = UrlConfig.getJenkinsRootURL();
        const orgsUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/scm/github/organizations/?credentialId=${credentialId}`, false);

        return this._fetch(orgsUrl)
            .then(credential => capabilityAugmenter.augmentCapabilities(credential));
    }

    listRepositories(credentialId, organizationName, pageNumber = 0, pageSize = 10) {
        const path = UrlConfig.getJenkinsRootURL();
        const reposUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/jenkins/scm/github/organizations/${organizationName}/repositories/` +
            `?credentialId=${credentialId}&pageNumber=${pageNumber}&pageSize=${pageSize}`);

        return this._fetch(reposUrl)
            .then(response => capabilityAugmenter.augmentCapabilities(response))
            .then(response => response.repositories.items);
    }

    createOrgFolder(credentialId, organization, repoNames = []) {
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/pipelines/`);

        const requestBody = this._buildRequestBody(
            true, credentialId, organization.name, organization.name, repoNames,
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

    _buildRequestBody(create, credentialId, itemName, organizationName, repoNames) {
        const className = create ?
            'GithubPipelineCreateRequest' :
            'GithubPipelineUpdateRequest';

        // TODO: credentialId?

        return {
            name: itemName,
            $class: `io.jenkins.blueocean.blueocean_github_pipeline.${className}`,
            scmConfig: {
                uri: 'https://api.github.com', // optional for github! required for enterprise where it should be http://ghe.acme.com/api/v3
                config: {
                    orgName: organizationName,
                    repos: repoNames,
                },
            },
        };
    }

}

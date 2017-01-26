import { capabilityAugmenter, Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';

export class GithubCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    listOrganizations(credentialId) {
        const path = UrlConfig.getJenkinsRootURL();
        const orgsUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/scm/github/organizations/?credentialId=${credentialId}`, false);

        return this._fetch(orgsUrl)
            .then(orgs => capabilityAugmenter.augmentCapabilities(orgs))
            .then(orgs => this.__addHalHrefs(orgs));
    }

    // TODO: temp method to add HAL href, for testing only
    __addHalHrefs(organizations) {
        return organizations.map(organization => {
            const org = organization;

            org._links.orgfolder = {
                _class: 'io.jenkins.blueocean.rest.hal.Link',
                href: `/blue/rest/organizations/jenkins/pipelines/${organization.name}/`,
            };

            return org;
        });
    }

    listRepositories(credentialId, organizationName, pageNumber = 1, pageSize = 100) {
        const path = UrlConfig.getJenkinsRootURL();
        const reposUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/jenkins/scm/github/organizations/${organizationName}/repositories/` +
            `?credentialId=${credentialId}&pageNumber=${pageNumber}&pageSize=${pageSize}`);

        return this._fetch(reposUrl)
            .then(response => capabilityAugmenter.augmentCapabilities(response));
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

    updateOrgFolder(credentialId, organization, repoNames = []) {
        const path = UrlConfig.getJenkinsRootURL();
        const { href } = organization._links.orgfolder;
        const updateUrl = Utils.cleanSlashes(`${path}/${href}`);

        const requestBody = this._buildRequestBody(
            false, credentialId, organization.name, organization.name, repoNames,
        );

        const fetchOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(updateUrl, { fetchOptions })
            .then(pipeline => capabilityAugmenter.augmentCapabilities(pipeline));
    }

    _buildRequestBody(create, credentialId, itemName, organizationName, repoNames) {
        const className = create ?
            'GithubPipelineCreateRequest' :
            'GithubPipelineUpdateRequest';

        return {
            credentialId,
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

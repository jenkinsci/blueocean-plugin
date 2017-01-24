import { Fetch } from '@jenkins-cd/blueocean-core-js';

function delayedPromise(value, delay = 2000) {
    return new Promise(resolve => {
        setTimeout(() => {
            resolve(value);
        }, delay);
    });
}

export default class GithubCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    listOrganizations() {
        return delayedPromise([
            { name: 'cliffmeyers' },
            { name: 'cloudbees' },
            { name: 'jenkinsci' },
        ]);
    }

    // eslint-disable-next-line
    listRepositories(organization) {
        return delayedPromise([
            { name: 'blueocean-plugin' },
            { name: 'jenkins-design-language' },
            { name: 'blueocean-acceptance-test' },
        ]);
    }

    createPipeline(organization, repository) {
        return this._createItem(organization, repository);
    }

    createOrgFolder(organization) {
        return this._createItem(organization);
    }

    // eslint-disable-next-line
    _createItem(org, repo) {
        return delayedPromise({}, 10000);

        /*
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/pipelines`);

        const requestBody = {
            name: org.name,
            $class: 'io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest',
            scmConfig: {
                config: {
                    orgName: org.name,
                },
            },
        };

        if (repo) {
            requestBody.scmConfig.config.repos = [repo.name];
        }

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(createUrl, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data));
        */
    }

}

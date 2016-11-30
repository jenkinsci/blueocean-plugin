/**
 * Created by cmeyers on 11/30/16.
 */
import { capabilityAugmenter, Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';

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

function delayedPromise(value, delay = 2000) {
    return new Promise(resolve => {
        setTimeout(() => {
            resolve(value);
        }, delay);
    });
}

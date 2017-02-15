import es6Promise from 'es6-promise'; es6Promise.polyfill();
import { Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';

/**
 * Proxy to the backend REST API.
 */
export default class GitCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    createPipeline(repositoryUrl, credentialId, name) {
        const path = UrlConfig.getRestBaseURL();
        const createUrl = Utils.cleanSlashes(`${path}/organizations/jenkins/pipelines`);

        const requestBody = {
            name,
            $class: 'io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest',
            scmConfig: {
                uri: repositoryUrl,
                credentialId,
            },
        };

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(createUrl, { fetchOptions });
    }

    checkPipelineNameAvailable(name) {
        const path = UrlConfig.getRestBaseURL();
        const checkUrl = Utils.cleanSlashes(`${path}/organizations/jenkins/pipelines/${name}`);

        const fetchOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return this._fetch(checkUrl, { fetchOptions })
            .then(() => false, () => true);
    }

}

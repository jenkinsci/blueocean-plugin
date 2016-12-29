import es6Promise from 'es6-promise'; es6Promise.polyfill();
import { Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';
import TempUtils from '../TempUtils';

/**
 * Proxy to the backend REST API.
 * Currently implemented as a mock.
 */
export default class GitCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    // eslint-disable-next-line no-unused-vars
    createPipeline(repositoryUrl, credentialId, pipelineName = null) {
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = TempUtils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/pipelines`);
        const name = !pipelineName ? repositoryUrl.split('/').slice(-1).join('') : pipelineName;

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

}

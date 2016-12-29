/**
 * Created by cmeyers on 10/19/16.
 */
import es6Promise from 'es6-promise'; es6Promise.polyfill();
import { capabilityAugmenter, Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';


/**
 * Proxy to the backend REST API.
 * Currently implemented as a mock.
 */
export default class GitCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    // eslint-disable-next-line no-unused-vars
    saveSshKeyCredential(key) {
        const credentialId = Math.random() * Number.MAX_SAFE_INTEGER;
        const promise = new Promise(resolve => {
            setTimeout(() => {
                resolve({
                    credentialId,
                });
            }, 2000);
        });

        return promise;
    }

    // eslint-disable-next-line no-unused-vars
    saveUsernamePasswordCredential(username, password) {
        return this.saveSshKeyCredential();
    }

    useSystemSshCredential() {
        return this.saveSshKeyCredential();
    }

    // eslint-disable-next-line no-unused-vars
    createPipeline(repositoryUrl, credentialId) {
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/pipelines`);
        const projectName = repositoryUrl.split('/').slice(-1).join('');

        const requestBody = {
            name: projectName,
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

        return this._fetch(createUrl, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data));
    }

}

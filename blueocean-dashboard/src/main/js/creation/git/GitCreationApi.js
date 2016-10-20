/**
 * Created by cmeyers on 10/19/16.
 */
import es6Promise from 'es6-promise'; es6Promise.polyfill();
// eslint-disable-next-line
import { Fetch } from '@jenkins-cd/blueocean-core-js';

export default class GitCreationApi {

    // eslint-disable-next-line
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

    saveUsernamePasswordCredential(username, password) {
        return this.saveSshKeyCredential();
    }

    useSystemSshCredential() {
        return this.saveSshKeyCredential();
    }

    // eslint-disable-next-line
    createPipeline(repositoryUrl, credentialId) {
        const uniqueId = Math.random() * Number.MAX_SAFE_INTEGER;
        const promise = new Promise(resolve => {
            setTimeout(() => {
                resolve({
                    pipelineId: `pipeline-${uniqueId}`,
                });
            }, 2000);
        });

        return promise;
    }

}

// @flow

import { Fetch, getRestUrl, sseService, loadingIndicator, capabilityAugmenter, RunApi } from '@jenkins-cd/blueocean-core-js';

const TIMEOUT = 60*1000;

export class SaveApi {

    _cleanup(sseId, timeoutId, onComplete, onError, err) {
        sseService.removeHandler(sseId);
        clearTimeout(timeoutId);
        loadingIndicator.hide();
        if (err && onError) {
            onError(err);
        } else {
            onComplete();
        }
    }

    _registerSse(timeoutId, onComplete, onError) {
        const sseId = sseService.registerHandler(event => {
            if (event.job_multibranch_indexing_result === 'SUCCESS') {
                this._cleanup(sseId, timeoutId, onComplete, onError);
            }
            if (event.job_multibranch_indexing_result === 'FAILURE') {
                this._cleanup(sseId, timeoutId, onComplete, onError, { message: 'Indexing failed' });
            }
        });
    }

    indexRepo(organization, teamName, repoName) {
        const createUrl = `${getRestUrl({organization})}/pipelines/`;

        const requestBody = {
            name: teamName,
            $class: 'io.jenkins.blueocean.blueocean_github_pipeline.GithubPipelineCreateRequest',
            scmConfig: {
                uri: 'https://api.github.com',
                config: {
                    orgName: teamName,
                    repos: [repoName],
                },
            },
        };

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return Fetch.fetchJSON(createUrl, { fetchOptions })
            .then(pipeline => capabilityAugmenter.augmentCapabilities(pipeline));
    }

    index(organization, folder, repo, complete, onError, progress) {
        loadingIndicator.show();
        const timeoutId = setTimeout(() => {
            this._cleanup(timeoutId, complete, onError);
        }, TIMEOUT);
        this._registerSse(timeoutId, complete, onError);
        this.indexRepo(organization, folder, repo);
    }

    /**
     * Indexes Multibranch pipeline
     * @param href URL of MBP pipeline
     * @param onComplete on success callback
     * @param onError on error callback
     */
    indexMbp(href, onComplete, onError) {
        loadingIndicator.show();
        const timeoutId = setTimeout(() => {
            this._cleanup(timeoutId, onComplete, onError);
        }, TIMEOUT);
        this._registerSse(timeoutId, onComplete, onError);
        RunApi.startRun({ _links: { self: { href: href + '/' }}})
            .catch(err => onError);
    }
}



const saveApi = new SaveApi();

export default saveApi;

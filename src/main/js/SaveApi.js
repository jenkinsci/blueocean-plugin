// @flow

import { action, computed, observable } from 'mobx';
import { Fetch, Paths, sseService, loadingIndicator } from '@jenkins-cd/blueocean-core-js';

export class SaveApi {
    index(organization, folder, complete, onError, progress) {
        const cleanup = err => {
            sseService.removeHandler(sseId);
            clearTimeout(timeoutId);
            loadingIndicator.hide();
            if (err && onError) {
                onError(err);
            } else {
                complete();
            }
        };
        
        loadingIndicator.show();
        
        const timeoutId = setTimeout(() => {
            cleanup();
        }, 60*1000);
        
        const sseId = sseService.registerHandler(event => {
            if (event.job_multibranch_indexing_result === 'SUCCESS') {
                if (progress) progress(event);
            }
            if (event.job_orgfolder_indexing_result === 'SUCCESS') {
                cleanup();
            }
            if (event.job_orgfolder_indexing_result === 'FAILURE') {
                cleanup({ message: 'Indexing failed' });
            }
        });

        Fetch.fetchJSON(Paths.rest.apiRoot() + '/organizations/' + organization + '/pipelines/' + folder + '/runs/1/replay/', {
            fetchOptions: {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: '{}',
            }
        })
        .then(data => {
            // Nothing to do here
        })
        .catch(err => {
            cleanup(err);
        });
    }
}

const saveApi = new SaveApi();

export default saveApi;

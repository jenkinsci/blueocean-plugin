import { sseService, loadingIndicator, RunApi } from '@jenkins-cd/blueocean-core-js';

const TIMEOUT = 60*1000;

export class SaveApi {

    /**
     * Indexes Multibranch pipeline
     * @param href URL of MBP pipeline
     * @param onComplete on success callback
     * @param onError on error callback
     */
    index(href, onComplete, onError) {
        loadingIndicator.show();
        const timeoutId = setTimeout(() => {
            this._cleanup(timeoutId, onComplete, onError);
        }, TIMEOUT);
        this._registerSse(timeoutId, onComplete, onError);
        RunApi.startRun({ _links: { self: { href: href + '/' }}})
            .catch(err => onError);
    }

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
}

const saveApi = new SaveApi();

export default saveApi;

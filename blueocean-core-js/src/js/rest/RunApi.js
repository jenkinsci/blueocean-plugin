/**
 * Created by cmeyers on 8/29/16.
 */
import fetch from 'isomorphic-fetch';

import { cleanSlashes } from '../sse/UrlUtils';
import config from '../sse/config';
config.loadConfig();

const defaultFetchOptions = {
    credentials: 'same-origin',
};

export class RunApi {

    startRun(item) {
        const path = config.jenkinsRootURL;
        const runUrl = cleanSlashes(`${path}/${item._links.self.href}/runs/`);

        const fetchOptions = {
            ...defaultFetchOptions,
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        // once job is queued, SSE will fire and trigger "updateRun" so no need to dispatch an action here
        return fetch(runUrl, fetchOptions);
    }

    stopRun(run) {
        const path = config.jenkinsRootURL;
        const runUrl = run._links.self.href;
        const stopUrl = cleanSlashes(`${path}/${runUrl}/stop/?blocking=true&timeOutInSecs=10`);

        const fetchOptions = {
            ...defaultFetchOptions,
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        // once job is queued, SSE will fire and trigger "updateRun" so no need to dispatch an action here
        return fetch(stopUrl, fetchOptions);
    }

}

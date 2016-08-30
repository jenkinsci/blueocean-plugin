/**
 * Created by cmeyers on 8/29/16.
 */
import isoFetch from 'isomorphic-fetch';

import { cleanSlashes } from '../sse/UrlUtils';
import config from '../sse/config';
config.loadConfig();

const defaultFetchOptions = {
    credentials: 'same-origin',
};

// TODO: remove all this code once JWT Fetch is integration
function checkStatus(response) {
    if (response.status >= 300 || response.status < 200) {
        const error = new Error(response.statusText);
        error.response = response;
        throw error;
    }
    return response;
}

function parseJSON(response) {
    return response.json()
    // FIXME: workaround for status=200 w/ empty response body that causes error in Chrome
    // server should probably return HTTP 204 instead
        .catch((error) => {
            if (error.message === 'Unexpected end of JSON input') {
                return {};
            }
            throw error;
        });
}

const fetch = (url, options) => {
    return isoFetch(url, options)
        .then(checkStatus)
        .then(parseJSON);
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

    replayRun(run) {
        const path = config.jenkinsRootURL;
        const runUrl = run._links.self.href;
        const replayPipelineUrl = cleanSlashes(`${path}/${runUrl}/replay/`);

        const fetchOptions = {
            ...defaultFetchOptions,
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return fetch(replayPipelineUrl, fetchOptions);
    }

}

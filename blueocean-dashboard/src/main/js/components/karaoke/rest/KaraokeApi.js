import { Fetch, FetchFunctions, logging } from '@jenkins-cd/blueocean-core-js';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.RestApi');

/**
 * Generic options needed for all requests
 * @type {{credentials: string, method: string}}
 */
const fetchOptionsCommon = {
    credentials: 'include',
    method: 'GET',
};
/**
 * Helper method to clone common fetchOptions
 * @param body - JSON object that we want to sent to the server
 * @returns {*} fetchOptions
 */
function prepareOptions(body) {
    const fetchOptions = Object.assign({}, fetchOptionsCommon);
    return fetchOptions;
}
export class KaraokeApi {

    /**
     * Start a run with parameters
     * @param href - the destination (ends normally with /runs/)
     * @param parameters - the parameters we want to submit
     * @returns {*} Promise
     */
    startRunWithParameters(href, parameters) {
        const fetchOptions = prepareOptions({ parameters });
        return Fetch.fetchJSON(href, { fetchOptions });
    }
}

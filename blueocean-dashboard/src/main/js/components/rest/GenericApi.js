import { capabilityAugmenter, Fetch, FetchFunctions, logging } from '@jenkins-cd/blueocean-core-js';

const logger = logging.logger('io.jenkins.blueocean.dashboard.branch.RestApi');

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
 * @returns {*} fetchOptions
 */
function prepareOptions() {
    const fetchOptions = Object.assign({}, fetchOptionsCommon);
    return fetchOptions;
}

export class GenericApi {

    getHref(href) {
        const fetchOptions = prepareOptions();
        logger.debug('Fetching href', href);
        return Fetch.fetchJSON(href, { fetchOptions })
            .then(FetchFunctions.checkStatus)
            .then(data => capabilityAugmenter.augmentCapabilities(data));
    }
}

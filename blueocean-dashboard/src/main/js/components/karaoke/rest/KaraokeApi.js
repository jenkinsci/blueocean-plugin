import { capabilityAugmenter, Fetch, logging } from '@jenkins-cd/blueocean-core-js';
import { generateDetailUrl } from '../urls/detailUrl';

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
 * @returns {*} fetchOptions
 */
function prepareOptions() {
    const fetchOptions = Object.assign({}, fetchOptionsCommon);
    return fetchOptions;
}
export class KaraokeApi {

    /**
     * Get a run with runId and augment the capabilities
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} runId Run that this pager belongs to.
     * @returns {*} Promise
     */
    getRunWithId(pipeline, branch, runId) {
        const fetchOptions = prepareOptions();
        const href = generateDetailUrl(pipeline, branch, runId);
        return Fetch.fetchJSON(href, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data));
    }
}

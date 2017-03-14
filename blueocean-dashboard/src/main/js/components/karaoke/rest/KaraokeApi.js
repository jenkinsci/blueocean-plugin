import { capabilityAugmenter, Fetch, FetchFunctions, logging } from '@jenkins-cd/blueocean-core-js';
import { generateDetailUrl } from '../urls/detailUrl';
import { getNodesInformation } from '../../../util/logDisplayHelper';

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

function parseMoreDataHeader(response) {
    let newStart = null;
    /*
     * If X-More-Data is true, then client should repeat the request after some delay.
     * In the repeated request it should use X-TEXT-SIZE header value with start query parameter.
     */
    if (response.headers.get('X-More-Data')) {
        /*
         * X-TEXT-SIZE is the byte offset of the raw log file client should use in the next request
         * as value of start query parameter.
         */
        newStart = response.headers.get('X-TEXT-SIZE');
    }
    response.newStart = newStart;  // eslint-disable-line
    return response;
}

function parseNewStart(response) {
    // By default only last 150 KB log data is returned in the response.
    const maxLength = 150000;
    const contentLength = Number(response.headers.get('X-Text-Size'));
    // set flag that there are more logs then we deliver
    const hasMore = contentLength > maxLength;
    response.hasMore = hasMore; // eslint-disable-line
    return response;
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
        logger.debug('Fetching href', href);
        return Fetch.fetchJSON(href, { fetchOptions })
            .then(FetchFunctions.checkStatus)
            .then(data => capabilityAugmenter.augmentCapabilities(data));
    }

    /**
     *
     * @param {string} href The url we want to fetch
     * @returns {*} Promise
     */
    getGeneralLog(href, { start }) {
        const fetchOptions = prepareOptions();
        const finalHref = start ? `${href}?start=${start}` : href;
        logger.debug('Fetching href', finalHref, start);
        return Fetch.fetch(finalHref, { fetchOptions })
            .then(FetchFunctions.checkStatus)
            .then(parseMoreDataHeader)
            .then(parseNewStart);
    }

    getNodes(href) {
        const fetchOptions = prepareOptions();
        logger.debug('Fetching href', href);
        return Fetch.fetchJSON(href, { fetchOptions })
            .then(FetchFunctions.checkStatus)
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(data => { 
                logger.warn('data dumb', data);
                return data;
            })
            .then(getNodesInformation);
    }

    getSteps(href) {
        return this.getNodes(href);
    }
}


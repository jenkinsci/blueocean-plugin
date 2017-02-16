import { observable, action, computed } from 'mobx';
import { Fetch, logging, capabilityAugmenter, capable } from '@jenkins-cd/blueocean-core-js';

import { FREESTYLE_JOB, PIPELINE_JOB } from '../../../Capabilities';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pager');

/**
 * The pager fetches pages of data from the BlueOcean api. It fetches pages of data, then
 * inserts them into the [@link BunkerService], and stores the href from the data.
 *
 * MobX computes a data field from the hrefs backed by the backend cache. This allows for SSE events
 * to be proporgated to the pager.
 *
 * @export
 * @class Pager
 */
export class Pager {
    /**
     * List of deisplayed items hrefs.
     */
    @observable href;
    /**
     * pager is fetching data.
     */
    @observable pending = false;
    /**
     * Will be set in an error occurs.
     */
    @observable error = null;
    /**
     * The latest page the pager has fetched.
     */
    @observable currentPage = 0;
    /**
     * More pages to fetch.
     */
    @observable hasMore = true;

    @observable isFreeStyle = false;
    @observable isPipeline = false;

    /**
     * Mobx computed value that creates an array of objects from the list of hrefs stored. If either the
     * bunker changes, or the hrefs change, this is recalculated and will trigger a react reaction.
     *
     * If item does not exist in bunker, then we just ignore it.
     * @readonly
     * @type {Array<Object>}
     */
    @computed
    get data() {
        logger.warn('returning data');
        return this.bunker.getItem(this.href);
    }
    /**
     * Creates an instance of Pager and fetches the first page.
     *
     * @param {string} url - Base url of collectin to fetch
     * @param {number} pageSize - Page size to fetch during one load.
     * @param {BunkerService} bunker - Data store
     * @param {UrlProvider} [urlProvider=paginateUrl]
     */
    constructor(url, bunker) {
        this.url = url;
        this.bunker = bunker;

        // Fetch the first page so that the user does not have to.
        this.fetchPage();
    }

    /**
     * Fetches the next page from the backend.
     *
     * @returns {Promise}
     */
    @action
    fetchPage() {
        // Get the next page's url.'

        this.pending = true;

        return Fetch.fetchJSON(this.url)
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(action('Process pager data', data => {
                // Store item in bunker.
                const saved = this.bunker.setItem(data);
                logger.warn('saved data', saved);
                // Append the new Href to the existing ones.
                debugger
                this.href = saved._links.self.href;
                this.isFreeStyle = capable(saved, FREESTYLE_JOB);
                this.isPipeline = capable(saved, PIPELINE_JOB);
                return this.isPipeline;
            }))
            .then(action('Process post data', isPipeline => {
                if (isPipeline) {
                    logger.debug('Here we need to do additional fetching');
                }
                this.pending = false;
            })).catch(err => {
                console.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }

    /**
     * Refreshes the Hrefs for the pager. It also stores the latest data in the [@link BunkerService]
     *
     * This might be called if something like sorting of a list changes.
     *
     * @returns {Promise}
     */
    @action
    refresh() {
        this.pending = true;
        return Fetch.fetchJSON(this.url) // Fetch data
            .then(action('set data', data => {
                this.bunker.setItems(data);
                this.href = data._links.self.href;
                this.pending = false;
            })).catch(err => {
                console.error('Error fetching page', err);
                this.err = err;
            });
    }

}

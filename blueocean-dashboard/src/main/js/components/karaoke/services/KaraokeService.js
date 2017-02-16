import { BunkerService, logging } from '@jenkins-cd/blueocean-core-js';
import { Pager } from './Pager';
import { generateDetailUrl } from '../urls/detailUrl';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Service');

/*
 * This class provides karaoke related services.
 *
 * @export
 * @class KaraokeService
 * @extends {BunkerService}
 */
export class KaraokeService extends BunkerService {
    /**
     * Generates a pager key for [@link PagerService] to store the [@link Pager] under.
     *
     * @param {string} organization Jenkins organization that this pager belongs to.
     * @param {string} pipeline Pipeline that this pager belongs to.
     * @param {string} run Run that this pager belongs to.
     * @returns {string} key for [@link PagerService]
     */
    pagerKey(pipeline, branch, runId) {
        return `Details/${pipeline.organization}-${pipeline.fullName}-${branch}-${runId}`;
    }

    /**
     * Gets the karaoke pager
     *
     * @param {string} organization Jenkins organization that this pager belongs to.
     * @param {string} pipeline Pipeline that this pager belongs to.
     * @returns {Pager} Pager for this pipelne.
     */
    karaokePager(pipeline, branch, runId) {
        this.pipeline = pipeline;
        this.branch = branch;
        this.runId = runId;
        this.idUrl = generateDetailUrl(pipeline, branch, runId);
        logger.warn(pipeline, branch, runId, this.idUrl);
        return this.pagerService.getPager({
            key: this.pagerKey(pipeline, branch, runId),
            /**
             * Lazily generate the pager incase its needed.
             */
            lazyPager: () => new Pager(this.idUrl, this),
        });
    }

    /**
     * Gets a detail from the store.
     *
     */
    getDetail(href) {
        return this.getItem(href);
    }

    setItems(items) {
        debugger
        this.setItem(items);
    }

    /**
     * Fetches a run from rest api.
     *
     * @param {string} href self href of node.
     * @param {boolean} useCache Use the cache to lookup data or always fetch a new one.
     * @returns {Promise} Promise of fetched data.
     */
    fetchDetails({ useCache } = {}) {
        logger.warn('debugger')
        if (useCache && this.hasItem(this.idUrl)) {
            return Promise.resolve(this.getItem(this.idUrl));
        }


        return Fetch.fetchJSON(this.idUrl)
            .then(data => {
                // Should really have dedupe on methods like these, but for now
                // just clone data so that we dont modify other instances.
                const detail = utils.clone(data);
                logger.warn('debugger')
                return this.setItem(detail);
            })
            .catch(err => {
                console.log('There has been an error while trying to get the data.', err);
            });
    }

}

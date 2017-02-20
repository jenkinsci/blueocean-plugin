import { BunkerService, logging } from '@jenkins-cd/blueocean-core-js';
import { Pager } from './Pager';

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
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} runId Run that this pager belongs to.
     * @returns {string} key for [@link PagerService]
     */
    pagerKey(pipeline, branch, runId) {
        return `Details/${pipeline.organization}-${pipeline.fullName}-${branch}-${runId}`;
    }
    /**
     * Gets the karaoke pager
     *
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} runId Run that this pager belongs to.
     * @returns {Pager} Pager for this pipelne.
     */
    karaokePager(pipeline, branch, runId) {
        return this.pagerService.getPager({
            key: this.pagerKey(pipeline, branch, runId),
            /**
             * Lazily generate the pager incase its needed.
             */
            lazyPager: () => new Pager(this, pipeline, branch, runId),
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
        this.setItem(items);
    }

}

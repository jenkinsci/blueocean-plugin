import { BunkerService, logging } from '@jenkins-cd/blueocean-core-js';
import { GeneralLogPager } from './pagers/GeneralLogPager';
import { PipelinePager } from './pagers/PipelinePager';

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
    generalLogPagerKey(pipeline, branch, runId) {
        const key = `GeneralLog/${pipeline.organization}-${pipeline.fullName}-${branch}-${runId}`;
        logger.debug('pagerKey:', key);
        return key;
    }
    /**
     * Gets the karaoke pager
     *
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} run Run that this pager belongs to.
     * @returns {Pager} Pager for this pipelne.
     */
    generalLogPager(augmenter, followAlong) {
        const { pipeline, branch, run } = augmenter;
        return this.pagerService.getPager({
            key: this.generalLogPagerKey(pipeline, branch, run.id),
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new GeneralLogPager(this, augmenter, followAlong),
        });
    }
    /**
     * Generates a pager key for [@link PagerService] to store the [@link Pager] under.
     *
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} runId Run that this pager belongs to.
     * @returns {string} key for [@link PagerService]
     */
    pipelinePagerKey(pipeline, branch, runId) {
        const key = `Pipeline/${pipeline.organization}-${pipeline.fullName}-${branch}-${runId}`;
        logger.debug('pagerKey:', key);
        return key;
    }
    /**
     * Gets the karaoke pager
     *
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} run Run that this pager belongs to.
     * @returns {Pager} Pager for this pipelne.
     */
    pipelinePager(augmenter, followAlong) {
        const { pipeline, branch, run } = augmenter;
        return this.pagerService.getPager({
            key: this.pipelinePagerKey(pipeline, branch, run.id),
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new PipelinePager(this, augmenter, followAlong),
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

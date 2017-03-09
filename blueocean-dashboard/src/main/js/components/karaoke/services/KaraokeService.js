import { BunkerService, logging } from '@jenkins-cd/blueocean-core-js';
import { GeneralLogPager, LogPager, PipelinePager } from './pagers';
const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Service');
/*
 * This class provides karaoke pager.
 *
 * @export
 * @class KaraokeService
 * @extends {BunkerService}
 */
export class KaraokePagerService extends BunkerService {
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
     * @param { object } augmenter
     * @returns {Pager} Pager for this pipelne.
     */
    generalLogPager(augmenter) {
        const { pipeline, branch, run } = augmenter;
        return this.pagerService.getPager({
            key: this.generalLogPagerKey(pipeline, branch, run.id),
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new GeneralLogPager(this, augmenter),
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
    logPagerKey(pipeline, branch, runId, stepId) {
        const key = `log/${pipeline.organization}-${pipeline.fullName}-${branch}-${runId}-step-${stepId}`;
        logger.debug('pagerKey:', key);
        return key;
    }
    /**
     * Gets the karaoke pager
     *
     * @param { object } augmenter
     * @returns {Pager} Pager for this pipelne.
     */
    logPager(augmenter, step) {
        const { pipeline, branch, run } = augmenter;
        return this.pagerService.getPager({
            key: this.logPagerKey(pipeline, branch, run.id, step.id),
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new LogPager(this, augmenter, step),
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
     * @param {object} augmenter
     * @param {boolean} followAlong
     * @param {object} node
     * @returns {Pager} Pager for this pipelne.
     */
    pipelinePager(augmenter, { node }) {
        const { pipeline, branch, run } = augmenter;
        return this.pagerService.getPager({
            key: this.pipelinePagerKey(pipeline, branch, run.id),
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new PipelinePager(this, augmenter, { node }),
        });
    }
}

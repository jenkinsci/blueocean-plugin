import { BunkerService, logging } from '@jenkins-cd/blueocean-core-js';
import { GeneralLogPager, LogPager, PipelinePager } from '.';
const logger = logging.logger('io.jenkins.blueocean.dashboard.following.Service');
/*
 * This class provides live update pagers
 *
 * @export
 * @class PagerProvider
 * @extends {BunkerService}
 */
export class PagerProvider extends BunkerService {
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
     * Gets the log pager
     *
     * @param { object } pipelineView
     * @returns {Pager} Pager for this pipelne.
     */
    generalLogPager(pipelineView, location) {
        const { pipeline, branch, run } = pipelineView;
        return this.pagerService.getPager({
            key: this.generalLogPagerKey(pipeline, branch, run.id),
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new GeneralLogPager(this, pipelineView, location),
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
     * Gets the log pager
     *
     * @param { object } pipelineView
     * @returns {Pager} Pager for this pipelne.
     */
    logPager(pipelineView, step) {
        const { pipeline, branch, run } = pipelineView;
        return this.pagerService.getPager({
            key: this.logPagerKey(pipeline, branch, run.id, step.id),
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new LogPager(this, pipelineView, step),
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
     * Gets the pipeline pager
     *
     * @param {object} pipelineView
     * @param {boolean} followAlong
     * @param {object} node
     * @returns {Pager} Pager for this pipelne.
     */
    pipelinePager(pipelineView, { node }) {
        const { pipeline, branch, run } = pipelineView;
        return this.pagerService.getPager({
            key: this.pipelinePagerKey(pipeline, branch, run.id),
            /**
             * Lazily generate the pager in case its needed.
             */
            lazyPager: () => new PipelinePager(this, pipelineView, node),
        });
    }
}

import { action, computed, observable } from 'mobx';
import { AppConfig, capable, logging } from '@jenkins-cd/blueocean-core-js';

import { KaraokeApi } from '../index';
import { FREESTYLE_JOB, MULTIBRANCH_PIPELINE, PIPELINE_JOB } from '../../../Capabilities';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pager');

function prefixIfNeeded(url) {
    return `${AppConfig.getJenkinsRootURL().replace(/\/$/, '')}${url}`;
}

/**
 * The pager fetches pages of data from the BlueOcean api. It fetches pages of data, then
 * inserts them into the [@link BunkerService], and stores the href from the data.
 *
 * MobX computes a data field from the href backed by the backend cache. This allows for SSE events
 * to be propagated to the pager.
 *
 * @export
 * @class Pager
 */
export class Pager {
    /**
     * The detail pager
     */
    @observable href;
    /**
     * pager is fetching data. log and detail
     * @type {boolean}
     */
    @observable pending = false;
    @observable logPending = false;
    /**
     * Will be set in an error occurs.
     * @type {object|null}
     */
    @observable error = null;
    /**
     * Do we have a free style job?
     * @type {boolean}
     */
    @observable isFreeStyle = false;
    /**
     * Do we have a pipeline job?
     * @type {boolean}
     */
    @observable isPipeline = false;
    /**
     * Do we have a multibranch pipeline job?
     * @type {boolean}
     */
    @observable isMultiBranchPipeline = false;
    /**
     * What is the general log url?
     * @type {string}
     */
    @observable generalLogUrl;

    @observable generalLogFileName;

    /**
     * Mobx computed value that creates an object. If either the  bunker changes,
     * or the href change, this is recalculated and will trigger a react reaction.
     *
     * If item does not exist in bunker, then we just ignore it.
     * @readonly
     * @type {object}
     */
    @computed
    get run() {
        return this.bunker.getItem(this.href);
    }
    /**
     * Mobx computed value that creates an object. If either the  bunker changes,
     * or the href change, this is recalculated and will trigger a react reaction.
     *
     * If item does not exist in bunker, then we just ignore it.
     * @readonly
     * @type {object}
     */
    @computed
    get log() {
        return this.bunker.getItem(this.generalLogUrl);
    }
    /**
     * Creates an instance of Pager and fetches the first page.
     *
     * @param {BunkerService} bunker - Data store
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} runId Run that this pager belongs to.
     */
    constructor(bunker, pipeline, branch, runId) {
        this.bunker = bunker;
        this.pipeline = pipeline;
        this.branch = branch;
        this.runId = runId;
        // Fetch the first page so that the user does not have to.
        this.fetchPage();
    }

    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchPage() {
        // while fetching we are pending
        this.pending = true;
        // get api data and further process it
        return KaraokeApi.getRunWithId(this.pipeline, this.branch, this.runId)
            .then(action('Process pager data', data => {
                // Store item in bunker.
                const saved = this.bunker.setItem(data);
                // Append the new Href to the existing ones.
                this.href = prefixIfNeeded(saved._links.self.href);
                logger.debug('saved data', this.href);
                this.generalLogUrl = prefixIfNeeded(saved._links.log.href);
                this.isFreeStyle = capable(saved, FREESTYLE_JOB);
                this.isPipeline = capable(saved, PIPELINE_JOB);
                this.isMultiBranchPipeline = capable(this.pipeline, MULTIBRANCH_PIPELINE);
                if (this.isMultiBranchPipeline) {
                    this.generalLogFileName = `${this.branch}-${this.runId}.txt`;
                } else {
                    this.generalLogFileName = `${this.runId}.txt`;
                }
                return this.isPipeline;
            }))
            .then(action('Process post data', isPipeline => {
                if (isPipeline) {
                    logger.debug('Here we need to do additional fetching');
                }
                this.pending = false;
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }


    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchGeneralLog({ start, followAlong }) {
        clearTimeout(this.timeout);
        // while fetching we are pending
        this.logPending = true;
        // log is text and not json, further it does not has _link in the response
        const logData = {
            _links: {
                self: {
                    href: this.generalLogUrl,
                },
            },
        };
        // get api data and further process it
        return KaraokeApi.getGeneralLog(this.generalLogUrl, { start })
            .then(response => {
                const { newStart, hasMore } = response;
                logger.warn({ newStart, hasMore });
                logData.hasMore = start === '0' ? false : hasMore;
                logData.newStart = newStart;
                return response.text();
            })
            .then(action('Process pager data', text => {
                if (text && text.trim) {
                    logData.data = text.trim().split('\n');
                }
                // Store item in bunker.
                this.bunker.setItem(logData);
                logger.debug('saved data', this.generalLogUrl, logData.newStart, followAlong);
                this.logPending = false;
                if (Number(logData.newStart) > 0 && followAlong) {
                    // kill current  timeout if any
                    clearTimeout(this.timeout);
                    // we need to get mpre input from the log stream
                    this.timeout = setTimeout(() => {
                        const props = { start: logData.newStart, followAlong };
                        logger.warn(props);
                        this.followGeneralLog(logData);
                    }, 1000);
                }
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }

    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    followGeneralLog(logDataOrg) {
        clearTimeout(this.timeout);
        const logData = { ...logDataOrg };
        // update link to trigger adding a new part of the log to the partial object
        // logData._links.self.href = `${this.generalLogUrl}?start=${logData.newStart}`;
        // while fetching we are pending
        this.logPending = true;
        logger.warn('changed ref', logData._links.self.href);
        return KaraokeApi.getGeneralLog(this.generalLogUrl, { start: logData.newStart })
            .then(action('Process pager data following 1', response => {
                const { newStart, hasMore } = response;
                logger.warn({ newStart, hasMore });
                logData.newStart = response.newStart;
                return response.text();
            }))
            .then(action('Process pager data following 2', text => {
                if (text && text.trim) {
                    logData.data = logData.data.concat(text.trim().split('\n'));
                }
                // Store item in bunker.
                this.bunker.setItem(logData);
                logger.debug('saved data', this.generalLogUrl, logData.newStart);
                this.logPending = false;
                if (logData.newStart !== null) {
                    // kill current  timeout if any
                    // we need to get mpre input from the log stream
                    this.timeout = setTimeout(() => {
                        this.followGeneralLog(logData);
                    }, 1000);
                }
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }


    clear() {
        clearTimeout(this.timeout);
    }
}

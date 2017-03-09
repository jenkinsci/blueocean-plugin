import { action, computed, observable } from 'mobx';
import { logging } from '@jenkins-cd/blueocean-core-js';

import { KaraokeApi } from '../../index';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pager');

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
export class LogPager {
    /**
     * pager is fetching data. log and detail
     * @type {boolean}
     */
    @observable pending = false;
    @observable used = false;
    /**
     * Will be set in an error occurs.
     * @type {object|null}
     */
    @observable error = null;
    @observable currentLogUrl;

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
        return this.bunker.getItem(this.currentLogUrl);
    }
    /**
     * Creates an instance of Pager and fetches the first page.
     *
     * @param {BunkerService} bunker - Data store
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} run Run that this pager belongs to.
     */
    constructor(bunker, augmenter, step) {
        this.bunker = bunker;
        this.augmenter = augmenter;
        this.step = step;
        if (step.isFocused && !step.isInputStep) { // we do not want to fetch when we have an input step
            this.fetchLog({
                followAlong: augmenter.karaoke,
                url: step.logUrl,
            });
        }
    }

    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchLog({ start, followAlong, url }) {
        clearTimeout(this.timeout);
        // while fetching we are pending
        this.pending = true;
        // log is text and not json, further it does not has _link in the response
        const logData = {
            _links: {
                self: {
                    href: url,
                },
            },
        };
        // get api data and further process it
        return KaraokeApi.getGeneralLog(url, { start })
            .then(response => {
                const { newStart, hasMore } = response;
                logger.debug({ newStart, hasMore });
                logData.hasMore = start === '0' ? false : hasMore;
                logData.newStart = newStart;
                return response.text();
            })
            .then(action('Process pager data', text => {
                if (text && text.trim) {
                    logData.data = text.trim().split('\n');
                    // Store item in bunker.
                    this.bunker.setItem(logData);
                    logger.debug('saved data', url, logData.newStart, followAlong);
                }
                this.currentLogUrl = url;
                this.pending = false;
                this.used = true;
                if (Number(logData.newStart) > 0 && followAlong) {
                    // kill current  timeout if any
                    clearTimeout(this.timeout);
                    // we need to get more input from the log stream
                    this.timeout = setTimeout(() => {
                        this.followLog(logData);
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
    followLog(logDataOrg) {
        clearTimeout(this.timeout);
        const logData = { ...logDataOrg };
        return KaraokeApi.getGeneralLog(logData._links.self.href, { start: logData.newStart })
            .then(action('Process pager data following 1', response => {
                const { newStart, hasMore } = response;
                logger.warn({ newStart, hasMore });
                logData.newStart = response.newStart;
                return response.text();
            }))
            .then(action('Process pager data following 2', text => {
                if (text && text.trim) {
                    const items = text.trim().split('\n');
                    logData.data = logData.data.concat(items);
                    // Store item in bunker.
                    this.bunker.setItem(logData);
                    logger.debug('saved data', logData._links.self.href, logData.newStart);
                }
                if (logData.newStart !== null) {
                    // kill current  timeout if any
                    // we need to get mpre input from the log stream
                    this.timeout = setTimeout(() => {
                        this.followLog(logData);
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

import { action, computed, observable } from 'mobx';
import { logging } from '@jenkins-cd/blueocean-core-js';

import { prefixIfNeeded } from '../../urls/prefixIfNeeded';
import { KaraokeApi } from '../../index';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pager.Pipeline');

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
export class PipelinePager {
    /**
     * pager is fetching data. log and detail
     * @type {boolean}
     */
    @observable pending = false;

    @observable currentNode = {};
    @observable currentStepsUrl;
    polling = false;
    /**
     * Will be set in an error occurs.
     * @type {object|null}
     */
    @observable error = null;

    /**
     * Mobx computed value that creates an object. If either the  bunker changes,
     * or the href change, this is recalculated and will trigger a react reaction.
     *
     * If item does not exist in bunker, then we just ignore it.
     * @readonly
     * @type {object}
     */
    @computed
    get nodes() {
        return this.bunker.getItem(this.augmenter.nodesUrl);
    }
    @computed
    get steps() {
        return this.bunker.getItem(this.currentStepsUrl);
    }
    /**
     * Creates an instance of Pager and fetches the first page.
     *
     * @param {BunkerService} bunker - Data store
     * @param {object} augmenter augmenter that this pager belongs to.
     * @param {object} props Properties that this pager belongs to.
     */
    constructor(bunker, augmenter, props) {
        this.bunker = bunker;
        this.augmenter = augmenter;
        this.fetchNodes({ ...props });
    }
    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchNodes({ node }) {
        logger.debug('Fetching now nodes url and further process it');
        // while fetching we are pending
        this.pending = true;
        // log is text and not json, further it does not has _link in the response
        const logData = {
            _links: {
                self: {
                    href: this.augmenter.nodesUrl,
                },
            },
        };
        // get api data and further process it
        return KaraokeApi.getNodes(this.augmenter.nodesUrl)
            .then(action('Process node data', result => {
                if (result.model.length === 0) {
                    logger.debug('Seems we do not have any nodes for this run.');
                    this.currentStepsUrl = this.augmenter.stepsUrl;
                    // we need now to fetch the steps
                    return this.fetchCurrentStepUrl();
                }
                // get information about the result
                logData.data = result;
                // compare whether we really need to
                // update the bunker
                const cached = this.bunker.getItem(logData._links.self.href);
                if (cached !== logData) { // calculate which node we need to focus
                    logger.debug('objects are different - updating store');
                    this.bunker.setItem(logData);
                }
                const focused = logData.data.model.filter((item) => {
                    if (node) {
                        logger.debug('check whether the node we are requesting is same', node, item);
                        return item.id === node;
                    }
                    return item.isFocused;
                })[0];
                // set either the focused node determined by the script or the last node
                if (focused) {
                    this.currentNode = focused;
                } else {
                    // Actually we should only come here on a not running job
                    logger.debug('Actually we should only come here on a not running job');
                    const lastNode = (logData.data.model[logData.data.model.length - 1]);
                    this.currentNode = lastNode;
                }
                this.currentStepsUrl = prefixIfNeeded(this.currentNode._links.steps.href);
                logger.debug('saved data', logData);
                return this.fetchCurrentStepUrl();
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }
    /**
     * Fetches the detail from the backend but only the nodes part
     *
     * @returns {Promise}
     */
    @action
    fetchNodesOnly() {
        logger.debug('Fetching now nodes url and further process it');
        // while fetching we are pending
        this.pending = true;
        // log is text and not json, further it does not has _link in the response
        const logData = {
            _links: {
                self: {
                    href: this.augmenter.nodesUrl,
                },
            },
        };
        // get api data and further process it
        return KaraokeApi.getNodes(this.augmenter.nodesUrl)
            .then(action('Process node data', result => {
                // get information about the result
                logData.data = result;
                // compare whether we really need to
                // update the bunker
                const cached = this.bunker.getItem(logData._links.self.href);
                if (cached !== logData) { // calculate which node we need to focus
                    logger.debug('objects are different - updating store');
                    this.bunker.setItem(logData);
                }
                this.pending = false;
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }
    @action
    fetchCurrentStepUrl() {
        this.pending = true;
        logger.debug('Fetching now current step url and further process it');
        clearTimeout(this.timeout);
        // log is text and not json, further it does not has _link in the response
        const logData = {
            _links: {
                self: {
                    href: this.currentStepsUrl,
                },
            },
        };
        // get api data and further process it
        return KaraokeApi.getSteps(this.currentStepsUrl)
            .then(action('Process steps data', result => {
                logData.data = result;
                const cached = this.bunker.getItem(logData._links.self.href);
                if (cached !== logData) { // calculate which node we need to focus
                    logger.debug('objects are different - updating store');
                    this.bunker.setItem(logData);
                    logger.debug('saved data');
                }
                this.pending = false;
                // we need to get more input from the log stream
                if (this.polling) {
                    logger.debug('follow along polling mode');
                    this.timeout = setTimeout(() => {
                        this.fetchCurrentStepUrl();
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

import { action, computed, observable } from 'mobx';
import { logging } from '@jenkins-cd/blueocean-core-js';

import { prefixIfNeeded } from '../../urls/prefixIfNeeded';
import { KaraokeApi } from '../../index';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.PipelinePager');

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
     * @param {object} props Properies that this pager belongs to.
     */
    constructor(bunker, augmenter, props) {
        this.bunker = bunker;
        this.augmenter = augmenter;
        this.fetchNodes({ ...props, followAlong: augmenter.karaoke });
    }

    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchNodes({ followAlong = false, node }) {
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
                    this.pending = false;
                    logger.debug('Seems we do not have any nodes for this run.');
                    // we need now to fetch the steps
                    return this.fetchSteps(followAlong);
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
                        return item.id === node;
                    }
                    return item.isFocused;
                })[0];

                // set either the focused node determined by the script or the last node
                if (focused) {
                    this.currentNode = focused;
                } else {
                    const lastNode = (logData.data.model[logData.data.model.length - 1]);
                    this.currentNode = lastNode;
                }
                this.currentStepsUrl = prefixIfNeeded(this.currentNode._links.steps.href);
                logger.warn('saved data', logData);
                this.pending = false;
                return this.fetchCurrentStepUrl(followAlong);
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }

    @action
    fetchCurrentStepUrl(followAlong) {
        clearTimeout(this.timeout);
        if (!followAlong) { // while fetching we are pending
            this.pending = true;
        }
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
                    logger.debug('saved data', followAlong);
                }
                // we need to get more input from the log stream
                if (false) {
                // if (followAlong) {
                    logger.debug('follow along');
                    this.timeout = setTimeout(() => {
                        const props = { followAlong };
                        logger.warn(props);
                        this.fetchCurrentStepUrl(followAlong);
                    }, 1000);
                }
                if (!followAlong) {
                    this.pending = false;
                }
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }
    @action
    fetchSteps(followAlong = false) {
        clearTimeout(this.timeout);
        if (!followAlong) { // while fetching we are pending
            this.pending = true;
        }
        // log is text and not json, further it does not has _link in the response
        const logData = {
            _links: {
                self: {
                    href: this.augmenter.stepsUrl,
                },
            },
        };
        // get api data and further process it
        return KaraokeApi.getSteps(this.augmenter.stepsUrl)
            .then(action('Process steps data', result => {
                this.currentStepsUrl = this.augmenter.stepsUrl;
                logData.data = result;
                const cached = this.bunker.getItem(logData._links.self.href);
                if (cached !== logData) { // calculate which node we need to focus
                    logger.debug('objects are different - updating store');
                    this.bunker.setItem(logData);
                    logger.debug('saved data', followAlong);
                }
                // we need to get more input from the log stream
                if (false) {
                // if (followAlong) {
                    logger.debug('follow along');
                    this.timeout = setTimeout(() => {
                        const props = { followAlong };
                        logger.warn(props);
                        this.fetchSteps(followAlong);
                    }, 1000);
                }
                if (!followAlong) {
                    this.pending = false;
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

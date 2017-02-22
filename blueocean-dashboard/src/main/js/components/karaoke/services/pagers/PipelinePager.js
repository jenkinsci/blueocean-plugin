import { action, computed, observable } from 'mobx';
import { logging } from '@jenkins-cd/blueocean-core-js';

import { prefixIfNeeded } from '../../urls/prefixIfNeeded';
import { KaraokeApi } from '../../index';
import { getNodesInformation } from '../../../../util/logDisplayHelper';

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
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} run Run that this pager belongs to.
     */
    constructor(bunker, augmenter, followAlong) {
        this.bunker = bunker;
        this.augmenter = augmenter;
        this.fetchNodes();
    }

    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchNodes(followAlong = false) {
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
                if (result.length === 0) {
                    logger.debug('Seems we do not have any nodes for this run.');
                    // we need now to fetch the steps
                    return this.fetchSteps(followAlong);
                } else {
                    // get information about the result
                    logData.data = getNodesInformation(result);
                    // calculate which node we need to focus
                    const focused = logData.data.model.filter((item) => item.isFocused)[0];
                    // set either the focused node determined by the script or the last node
                    if (focused) {
                        this.currentNode = focused;
                    } else {
                        this.currentNode = (logData.data.model[logData.data.model.length - 1]);
                    }
                    this.currentStepsUrl = prefixIfNeeded(this.currentNode._links.steps.href);
                    this.bunker.setItem(logData);
                    this.fetchCurrentStepUrl();
                    logger.warn('saved data', logData);
                }
                this.pending = false;
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }

    @action
    fetchCurrentStepUrl(followAlong) {
        // while fetching we are pending
        this.pending = true;
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
                logData.data = getNodesInformation(result);
                this.bunker.setItem(logData);
                logger.debug('saved data');
                this.pending = false;
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }
    @action
    fetchSteps(followAlong) {
        clearTimeout(this.timeout);
        // while fetching we are pending
        this.pending = true;
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
                logData.data = getNodesInformation(result);
                this.bunker.setItem(logData);
                logger.debug('saved data');
                this.pending = false;
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }

    clear() {
        clearTimeout(this.timeout);
    }
}

import { action, observable } from 'mobx';
import { Logger } from '../../../../util/Logger';

import { KaraokeApi } from '../../index';

const log = new Logger('pipeline.run.pager');

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
    getNodes(pipelineView) {
        return this.bunker.getItem(pipelineView.nodesUrl);
    }

    getSteps(pipelineView) {
        return this.stepsUrl && this.bunker.getItem(pipelineView.stepsUrl);
    }

    /**
     * Creates an instance of Pager and fetches the first page.
     *
     * @param {BunkerService} bunker - Data store
     */
    constructor(bunker, nodesUrl) {
        this.bunker = bunker;
        this.nodesUrl = nodesUrl;
    }

    /**
     * Gets the default focused node
     * @returns {object} the node
     */
    getDefaultFocusedNode(pipelineView) {
        const nodeData = this.bunker.getItem(pipelineView.nodesUrl);
        if (!nodeData) {
            throw new Error('No node:');
        }
        // set either the active node determined by the script or the last node
        for (const node of nodeData.data.model) {
            if (node.isFailingNode || (!node.isCompleted && node.edges.length === 0)) {
                return node;
            }
        }
        return nodeData.data.model[nodeData.data.model.length - 1];
    }

    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchNodes(pipelineView) {
        log.debug('fetchNodes for', pipelineView.nodesUrl);
        // while fetching we are pending
        this.pending = true;
        // get api data and further process it
        return KaraokeApi.getNodes(pipelineView.nodesUrl)
            .then(result => {
                const nodeData = {
                    _links: {
                        self: {
                            href: pipelineView.nodesUrl,
                        },
                    },
                };
                // get information about the result
                nodeData.data = result;
                this.setNodeData(nodeData);
                return nodeData._links.self.href;
            }).catch(err => {
                log.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }

    @action
    setNodeData(nodeData) {
        log.debug('setNodeData', nodeData);
        this.bunker.setItem(nodeData);
    }

    @action
    fetchCurrentSteps() {
        this.pending = true;
        log.info('fetchCurrentSteps', this.stepsUrl);
        clearTimeout(this.timeout);
        // get api data and further process it
        return KaraokeApi.getSteps(this.stepsUrl)
            .then(action('Process steps data', result => {
                // log is text and not json, further it does not has _link in the response
                const stepData = {
                    _links: {
                        self: {
                            href: this.stepsUrl,
                        },
                    },
                };
                stepData.data = result;
                const cached = this.bunker.getItem(stepData._links.self.href);
                if (cached !== stepData) { // calculate which node we need to focus
                    log.info('objects are different - updating store');
                    this.bunker.setItem(stepData);
                    log.info('saved data');
                }
                this.pending = false;
            })).catch(err => {
                log.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }

    clear() {
        clearTimeout(this.timeout);
    }
}

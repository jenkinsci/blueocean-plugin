console.log('ns');

import { action, observable } from 'mobx';
import { Logger } from '../../../util/Logger';
import { KaraokeApi } from '../index';
import cache from './DataCache';

const log = new Logger('pipeline.run.store.nodes');

export class NodeStore {
    @observable pending = false;
    @observable nodes;
    @observable error;
    url;

    constructor(url) {
        this.url = url;
        this.setNodes(cache.get(this.url) || {
            _links: {
                self: {
                    href: this.url,
                },
            },
            data: null,
        });
    }

    @action
    setNodes(nodes) {
        this.nodes = nodes;
        cache.put(this.url, this.nodes);
    }

    @action
    setNodeData(data) {
        this.nodes.data = data;
        cache.put(this.url, this.nodes);
    }

    @action
    setError(error) {
        this.error = error;
    }

    @action
    setPending(pending) {
        this.pending = pending;
    }

    getNodeData() {
        const nodeData = this.nodes.data;
        if (!nodeData) {
            throw new Error('No node data yet');
        }
        return nodeData;
    }

    /**
     * Gets the default focused node
     * @returns {object} the node
     */
    findAutoFocusNode() {
        const nodeData = this.getNodeData();
        // set either the active node determined by the script or the last node
        for (const node of nodeData.model) {
            if (node.isFailingNode || (!node.isCompleted && node.edges.length === 0)) {
                return node;
            }
        }
        // otherwise focus on the last node
        return nodeData.model[nodeData.model.length - 1];
    }

    findNode(nodeId) {
        return this.getNodeData().model.filter((item) => item.id === nodeId)[0];
    }

    /**
     * Fetches the detail from the backend and set the data
     * @returns {Promise}
     */
    fetch() {
        log.debug('fetching log', this.url);
        this.setPending(true);
        return KaraokeApi.getNodes(this.url)
            .then(result => {
                this.setNodeData(result);
            }).catch(err => {
                log.error('Error fetching nodes', err);
                this.setError(err);
            }).then(() => {
                this.setPending(false);
            });
    }
}

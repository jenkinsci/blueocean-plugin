import React, { Component, PropTypes } from 'react';
import { PipelineGraph } from '@jenkins-cd/design-language';
import { TimeManager, i18nTranslator, logging } from '@jenkins-cd/blueocean-core-js';

const timeManager = new TimeManager();
const { array, any, func, object, string } = PropTypes;
const logger = logging.logger('io.jenkins.blueocean.dashboard.PipelineRunGraph');
const translate = i18nTranslator('blueocean-web');

function badNode(jenkinsNode) {
    // eslint-disable-next-line
    console.error('Malformed / missing Jenkins run node:', jenkinsNode);
    return new Error('convertJenkinsNodeDetails: malformed / missing Jenkins run node.');
}

function convertJenkinsNodeDetails(jenkinsNode, isCompleted, skewMillis = 0) {
    if (!jenkinsNode
        || !jenkinsNode.id) {
        throw badNode(jenkinsNode);
    }
    logger.debug('jenkinsNode', jenkinsNode);
    const isRunning = () => {
        switch (jenkinsNode.state) {
        case 'RUNNING':
        case 'PAUSED':
        case 'QUEUED':
            return true;
        default:
            return false;
        }
    };

    const { durationInMillis, startTime } = jenkinsNode;

    // we need to make sure that we calculate with the correct time offset
    const harmonized = timeManager.harmonizeTimes({
        isRunning: isRunning(),
        durationInMillis,
        startTime,
    }, skewMillis);
    let completePercent = 0;
    let state = 'unknown';

    if (jenkinsNode.result === 'SUCCESS') {
        state = 'success';
        completePercent = 100;
    } else if (jenkinsNode.result === 'FAILURE') {
        state = 'failure';
        completePercent = 100;
    } else if (jenkinsNode.state === 'PAUSED') {
        state = 'paused';
        completePercent = 100;
    } else if (jenkinsNode.result === 'UNSTABLE') {
        state = 'unstable';
        completePercent = 100;
    } else if (jenkinsNode.result === 'ABORTED') {
        state = 'aborted';
        completePercent = 100;
    } else if (jenkinsNode.state === 'SKIPPED') {
        state = 'skipped';
        completePercent = 0;
    } else if (jenkinsNode.state === 'RUNNING') {
        state = 'running';
        completePercent = 50;
    } else if (jenkinsNode.state === 'QUEUED'
        || (jenkinsNode.state === null && !isCompleted)) {
        state = 'queued';
        completePercent = 0;
    } else if (jenkinsNode.state === 'NOT_BUILT'
        || jenkinsNode.state == null) {
        state = 'not_built';
        completePercent = 0;
    }
    const i18nDuration = timeManager.format(harmonized.durationInMillis, translate('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' }));

    const title = state === 'running' ? '' : translate(`common.state.${state}`, { 0: i18nDuration });

    const converted = {
        name: jenkinsNode.displayName,
        children: [],
        state,
        completePercent,
        id: jenkinsNode.id,
        title,
        type: jenkinsNode.type,
    };
    logger.debug('converted node', converted);
    return converted;
}

/**
 * Convert the graph results of a run as reported by Jenkins into the  model required by the PipelineGraph component
 *
 * We need isCompleted (referring to the entire pipeline) to determine wether nodes that haven't been run are still
 * pending or simply weren't executed due to pipeline logic (skipped) or early-abort (either failure or intervention)
 */
export function convertJenkinsNodeGraph(jenkinsGraph, isCompleted, skewMillis) {
    if (!jenkinsGraph || !jenkinsGraph.length) {
        return [];
    }

    const results = [];
    const originalNodeForId = {};
    const convertedNodeForId = {};
    const allEdges = []; // Array of [srcId, destId] pairs
    const edgeCountToNode = {}; // id => int
    // const edgeCountFromNode = {}; // id => int
    let firstNode = undefined;

    // Convert the basic details of nodes, and index them by id
    jenkinsGraph.forEach(jenkinsNode => {
        const convertedNode = convertJenkinsNodeDetails(jenkinsNode, isCompleted, skewMillis);
        const { id } = convertedNode;

        firstNode = firstNode || convertedNode;
        convertedNodeForId[id] = convertedNode;
        originalNodeForId[id] = jenkinsNode;
        edgeCountToNode[id] = 0;
        // edgeCountFromNode[id] = 0;

        for (const edge of jenkinsNode.edges) {
            allEdges.push([id, edge.id]);
        }
    });

    // Filter out any edges to missing nodes
    allEdges.filter(([src, dest]) => (src in convertedNodeForId && dest in convertedNodeForId));

    // Cound edges going to/from each node.
    for (const edgePair of allEdges) {
        const dest = edgePair[1];
        edgeCountToNode[dest] = edgeCountToNode[dest] + 1;
    }

    // Follow the graph and build our results
    let currentNode = firstNode;
    while (currentNode) {
        results.push(currentNode);

        let nextNode = null;
        const originalNode = originalNodeForId[currentNode.id];
        const edges = originalNode.edges || [];
        let parallelNodes = [];
        if (edges.length > 0) {
            parallelNodes = edges.filter(edge => edge.type === 'PARALLEL');
        }

        if (edges.length === 1 && parallelNodes.length === 0) {
            // Single following (sibling) node
            nextNode = convertedNodeForId[edges[0].id];
        } else if (parallelNodes.length > 0) {
            // Multiple following nodes are child nodes (parallel branch) not siblings

            // Put the first node of each branch into the children
            currentNode.children = parallelNodes
                .map(edge => convertedNodeForId[edge.id])
                .filter(node => !!node);

            // Now follow the edges along until they coalesce again, which will be the next top-level stage
            let branchNodes = currentNode.children;

            while (branchNodes && branchNodes.length > 0) {
                const nextBranchNodes = [];

                for (const branchNode of branchNodes) {
                    const branchNodeEdges = originalNodeForId[branchNode.id].edges || [];
                    if (branchNodeEdges.length > 0) { // Should only be 0 at end of pipeline or bad input data
                        const followingNode = convertedNodeForId[branchNodeEdges[0].id];

                        // If followingNode has several edges pointing to it....

                        if (branchNodeEdges[0].type === 'STAGE') {
                            // ... then it's the next top-level stage so we're done following this parallel branch...
                            nextNode = followingNode;
                        } else {
                            // ... otherwise it's the next sibling stage within this parallel branch.
                            branchNode.nextSibling = followingNode;
                            nextBranchNodes.push(followingNode);
                        }
                    }
                }

                branchNodes = nextBranchNodes;
            }
        }

        currentNode = nextNode;
    }

    return results;
}

export default class PipelineRunGraph extends Component {

    constructor(props) {
        super(props);
        this.lastData = null;
        this.state = { graphNodes: null };
    }

    componentWillMount() {
        const { nodes, run } = this.props;
        this.processData(nodes, run);
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.nodes !== this.lastData || nextProps.run !== this.props.run) {
            this.processData(nextProps.nodes, nextProps.run);
        }
    }

    processData(newData, run) {
        this.lastData = newData;
        const isCompleted = run.state.toUpperCase() === 'FINISHED';
        const skewMillis = this.context.config.getServerBrowserTimeSkewMillis();
        const convertedGraph = convertJenkinsNodeGraph(newData, isCompleted, skewMillis);

        this.setState({
            graphNodes: convertedGraph,
        });
    }

    graphNodeClicked = (name, stageId) => {
        const { callback } = this.props;
        if (callback) {
            callback(stageId);
        }
    };

    render() {
        const { graphNodes, t } = this.state;

        if (!graphNodes) {
            // FIXME: Make a placeholder empty state when nodes is null (loading)
            return (<div>{t('common.pager.loading', {
                defaultValue: 'Loading...',
            })}</div>);
        } else if (graphNodes.length === 0) {
            // Do nothing when there's no nodes
            return null;
        }

        const id = this.props.selectedStage.id;

        let selectedStage = null;

        // Find selected stage by id
        for (const topStage of graphNodes) {
            if (topStage.id === id) {
                selectedStage = topStage;
            } else {
                for (const child of topStage.children) {
                    if (child.id === id) {
                        selectedStage = child;
                        break;
                    }
                }
            }
            if (selectedStage) {
                break;
            }
        }

        return (
            <div className="PipelineGraph-container">
                <PipelineGraph stages={graphNodes}
                               selectedStage={selectedStage}
                               onNodeClick={this.graphNodeClicked}
                />
            </div>
        );
    }
}

PipelineRunGraph.propTypes = {
    pipelineName: string,
    branchName: string,
    run: object,
    nodes: array,
    node: any,
    selectedStage: object,
    callback: func,
};


PipelineRunGraph.contextTypes = {
    config: object.isRequired,
};

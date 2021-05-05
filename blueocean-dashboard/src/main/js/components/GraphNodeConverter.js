import { i18nTranslator, logging, TimeManager } from '@jenkins-cd/blueocean-core-js';

const timeManager = new TimeManager();
const logger = logging.logger('io.jenkins.blueocean.dashboard.PipelineRunGraph');
const translate = i18nTranslator('blueocean-web');

function badNode(jenkinsNode) {
    // eslint-disable-next-line
    console.error('Malformed / missing Jenkins run node:', jenkinsNode);
    return new Error('convertJenkinsNodeDetails: malformed / missing Jenkins run node.');
}

function convertJenkinsNodeDetails(jenkinsNode, isCompleted, skewMillis = 0) {
    if (!jenkinsNode || !jenkinsNode.id) {
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
    const harmonized = timeManager.harmonizeTimes(
        {
            isRunning: isRunning(),
            durationInMillis,
            startTime,
        },
        skewMillis
    );
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
    } else if (jenkinsNode.state === 'QUEUED' || (jenkinsNode.state === null && !isCompleted)) {
        state = 'queued';
        completePercent = 0;
    } else if (jenkinsNode.state === 'NOT_BUILT' || jenkinsNode.state == null) {
        state = 'not_built';
        completePercent = 0;
    }
    const i18nDuration = timeManager.format(
        harmonized.durationInMillis,
        translate('common.date.duration.hint.format', { defaultValue: 'M [month], d [days], h[h], m[m], s[s]' })
    );

    const title = state === 'running' ? '' : translate(`common.state.${state}`, { 0: i18nDuration });

    const converted = {
        name: jenkinsNode.displayName,
        children: [],
        state,
        completePercent,
        id: jenkinsNode.id,
        title,
        type: jenkinsNode.type,
        seqContainerName: '',
    };
    logger.debug('converted node', converted);
    return converted;
}

function buildSequentialStages(originalNodes, convertedNodes, sequentialNodeKey, currentNode) {
    const nextSequentialNodeId =
        originalNodes[sequentialNodeKey].edges.length && originalNodes[sequentialNodeKey].edges[0] ? originalNodes[sequentialNodeKey].edges[0].id : '';

    currentNode.isSequential = true;
    if (nextSequentialNodeId) {
        if (
            originalNodes[sequentialNodeKey].edges.length &&
            originalNodes[nextSequentialNodeId] &&
            originalNodes[nextSequentialNodeId].firstParent == currentNode.id
        ) {
            currentNode.nextSibling = convertedNodes[nextSequentialNodeId];

            buildSequentialStages(originalNodes, convertedNodes, currentNode.nextSibling.id, currentNode.nextSibling);
        }
    }
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
    allEdges.filter(([src, dest]) => src in convertedNodeForId && dest in convertedNodeForId);

    // Count edges going to/from each node.
    for (const edgePair of allEdges) {
        const dest = edgePair[1];
        edgeCountToNode[dest] = edgeCountToNode[dest] + 1;
    }

    // Follow the graph and build our results
    let currentNode = firstNode;
    while (currentNode) {
        if (!currentNode.isSequential) {
            results.push(currentNode);
        }

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
            currentNode.children = parallelNodes.map(edge => convertedNodeForId[edge.id]).filter(node => !!node);

            // Now follow the edges along until they coalesce again, which will be the next top-level stage
            let branchNodes = currentNode.children;

            while (branchNodes && branchNodes.length > 0) {
                const nextBranchNodes = [];

                for (const branchNode of branchNodes) {
                    const branchNodeEdges = originalNodeForId[branchNode.id].edges || [];

                    Object.keys(convertedNodeForId).map((key, index) => {
                        //Check if this stage contains sequential stages and if so, replace it with the first one in the sequence
                        if (originalNodeForId[key].firstParent === branchNode.id) {
                            for (var i = 0; i < currentNode.children.length; i++) {
                                if (currentNode.children[i].id === branchNode.id) {
                                    convertedNodeForId[key].seqContainerName = branchNode.name;
                                    currentNode.children[i] = convertedNodeForId[key];
                                    if (originalNodeForId[key].edges.length && originalNodeForId[key].edges[0]) {
                                        buildSequentialStages(originalNodeForId, convertedNodeForId, key, currentNode.children[i]);
                                    } else {
                                        //this happens if the sequential stage group only has one stage
                                        currentNode.children[i].isSequential = true;
                                    }

                                    break;
                                }
                            }
                        }
                    });

                    if (branchNodeEdges.length > 0) {
                        // Should only be 0 at end of pipeline or bad input data
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

import { capable } from '@jenkins-cd/blueocean-core-js';
import { convertJenkinsNodeGraph } from '../../GraphNodeConverter';
import { StageInfo } from '@jenkins-cd/pipeline-graph-widget/dist/lib';

const RESULTS = {
    UNKNOWN: 'UNKNOWN',
    SUCCESS: 'SUCCESS',
    FAILURE: 'FAILURE',
};

const STATES = {
    RUNNING: 'RUNNING',
    PAUSED: 'PAUSED',
    FINISHED: 'FINISHED',
};

function isRunningNode(item) {
    return item.state === STATES.RUNNING || item.state === STATES.PAUSED;
}

/*
    NB: These types aren't canonical, they're inferred from existing code and data examples,
    and only show what we need for this module.
 */

interface Action {
    name: string;
    urlName: string;
    _links: Links;
}

interface Edge {
    id: string;
    type: string;
}

interface Links {
    self: Link;
    [name: string]: Link;
}

interface Link {
    href: string;
}

type APIResult = 'SUCCESS' | 'UNSTABLE' | 'FAILURE' | 'NOT_BUILT' | 'UNKNOWN' | 'ABORTED';

type APIState = 'QUEUED' | 'RUNNING' | 'PAUSED' | 'SKIPPED' | 'NOT_BUILT' | 'FINISHED';

type APIInput = object; // TODO: Specify? Upgrade TS and use unknown?

interface APINode {
    actions: Array<Action>;
    _links: Links;
    id: string;
    displayName: string;
    result: APIResult | null;
    state: APIState | null;
    displayDescription: string | null;
    edges: Array<Edge>;
    firstParent: string | null; // Immediately previous node within the graph, could be a container or sibling
    restartable: boolean;
    estimatedDurationInMillis?: number;
    durationInMillis: number;
    type: string;
    startTime: string; // ISO
    input: APIInput | null;
    causeOfBlockage: string | null;
}

interface ModelNode {
    // Calculated
    key: string;
    title: string;
    hasLogs: boolean;
    logUrl?: string;
    isInputStep: boolean;
    isParallel: boolean; // Set for solo-stage parallels
    isSequential: boolean; // Set for multi-stage parallel branch
    parent: string | null; // Containing stop stage node id for parallels, or previous top-level stage for top-level stages
    isRunning: boolean;
    isCompleted: boolean; // Running and completed are separate, because a job might be paused or waiting for resources or input
    computedResult: APIResult | APIState;
    isFocused?: boolean;
    isMultiBranch?: boolean;

    // Promoted from APINode / StageInfo
    actions: Array<Action>;
    _links: Links;
    id: string;
    displayName: string;
    result: APIResult | null;
    state: APIState | null;
    displayDescription: string | null;
    edges: Array<Edge>;
    firstParent?: string;
    restartable: boolean;
    estimatedDurationInMillis?: number;
    durationInMillis?: number;
    type: string;
    startTime?: string; // ISO
    input?: APIInput;
    seqContainerName?: string;
}

interface NodesInformation {
    isFinished: boolean;
    hasResultsForSteps: boolean;
    model: Array<ModelNode>;
    isError: boolean | null;
    runningNodes?: Array<string>;
    errorNodes?: Array<string>;
}

function contains(array, val) {
    if (array.contains) {
        return array.contains(val);
    }
    return array.indexOf(val) !== -1;
}

/**
 * The pipeline result screen uses a more complex model of nodes than just what the graph renderer itself needs, so this is where
 * we generate that model.
 */
export function getNodesInformation(nodes: Array<APINode>): NodesInformation {
    let model: Array<ModelNode> = [];

    //--------------------------------------
    // Classify and index nodes by id
    //--------------------------------------

    let allNodeIds: Array<string> = [];
    let runningNodeIds: Array<string> = [];
    let errorNodeIds: Array<string> = [];
    let queuedNodeIds: Array<string> = [];
    let hasResultsForSteps = false;
    let isError = false;

    for (const node of nodes) {
        allNodeIds.push(node.id);

        if (isRunningNode(node) && (!node.edges || node.edges.length < 2)) {
            runningNodeIds.push(node.id);
        }

        if (node.result == RESULTS.FAILURE) {
            errorNodeIds.push(node.id);
            isError = true;
        }

        if (node.state == null && node.result == null) {
            queuedNodeIds.push(node.id);
        } else {
            hasResultsForSteps = true; // A single non-queued stage is enough
        }
    }

    const isFinished = runningNodeIds.length === 0 && queuedNodeIds.length !== nodes.length;

    //--------------------------------------
    // Convert APINodes to ModelNodes
    //--------------------------------------

    // Run the graph through the renderer's conversion process. We don't generate the ModelNodes from it, but
    // we use its hierarchy, and we'll hand it down to be used by the graph component later.

    const convertedGraph: Array<StageInfo> = convertJenkinsNodeGraph(nodes, isFinished, 0); // TODO: Skewmillis from where?

    let lastTopStageId: string | null = null;

    // Walk the hierarchical structure in order, generating ModelNodes from the APINodes as we go
    for (const topStage of convertedGraph) {
        const topStageId = String(topStage.id);
        const topStageAPINode = nodes[allNodeIds.indexOf(topStageId)];

        model.push(makeModelNode(topStageAPINode, lastTopStageId, contains(runningNodeIds, topStageId)));

        // For each branch, we make nodes for the initial stage and any sequentials following it
        for (const firstNodeForBranch of topStage.children) {
            let aGraphNode: StageInfo | undefined = firstNodeForBranch;

            while (aGraphNode) {
                const anAPINode = nodes[allNodeIds.indexOf(String(aGraphNode.id))];
                model.push(makeModelNode(anAPINode, topStageId, contains(runningNodeIds, anAPINode.id), aGraphNode.seqContainerName));

                aGraphNode = aGraphNode.nextSibling;
            }
        }

        lastTopStageId = topStageId;
    }

    //--------------------------------------
    // Choose a node to focus on initially
    //--------------------------------------

    let wasFocused = false;

    // ... look for the first running node
    for (const node of model) {
        if (node.isRunning) {
            wasFocused = true;
            node.isFocused = true;
            break;
        }
    }

    // ... failing that the first error node
    if (!wasFocused) {
        for (const node of model) {
            if (contains(errorNodeIds, node.id)) {
                wasFocused = true;
                node.isFocused = true;
                break;
            }
        }
    }

    // ... otherwise just focus on the first node
    if (!wasFocused && model.length) {
        model[0].isFocused = true;
    }

    //--------------------------------------
    // Sum up and return all our new knowledge
    //--------------------------------------

    const information: NodesInformation = {
        isFinished,
        hasResultsForSteps,
        model,
        // TODO: convertedGraph,
        isError: null,
    };

    // on not finished we return null and not a bool since we do not know the result yet
    if (!isFinished) {
        information.isError = null;
    } else {
        information.isError = isError;
    }

    if (!isFinished) {
        information.runningNodes = runningNodeIds;
    } else if (isError) {
        information.errorNodes = errorNodeIds;
    }

    return information;
}

function makeModelNode(apiNode: APINode, parent: string | null, isRunning: boolean, seqContainerName?: string): ModelNode {
    const title = apiNode.displayDescription ? apiNode.displayName + ': ' + apiNode.displayDescription : apiNode.displayName;

    const logActions = apiNode.actions ? apiNode.actions.filter(action => capable(action, 'org.jenkinsci.plugins.workflow.actions.LogAction')) : [];
    const hasLogs = logActions.length > 0;

    const isParallel = apiNode.type === 'PARALLEL';
    const isSequential = (apiNode.firstParent && apiNode.firstParent !== parent) || false;
    const isCompleted = apiNode.result !== 'UNKNOWN' && apiNode.result !== null;
    const computedResult = (isCompleted ? apiNode.result : apiNode.state) || 'UNKNOWN';
    const isInputStep = !!apiNode.input;

    const modelNode: ModelNode = {
        actions: apiNode.actions,
        _links: apiNode._links,
        key: 'stageNode_' + apiNode.id,
        id: apiNode.id,
        edges: apiNode.edges,
        type: apiNode.type,
        displayName: apiNode.displayName,
        displayDescription: apiNode.displayDescription,
        title: title || `runId: ${apiNode.id}`,
        durationInMillis: apiNode.durationInMillis || undefined,
        startTime: apiNode.startTime || undefined,
        result: apiNode.result,
        state: apiNode.state,
        restartable: apiNode.restartable,
        hasLogs,
        logUrl: hasLogs ? logActions[0]._links.self.href : undefined,
        isParallel,
        isSequential,
        parent,
        firstParent: apiNode.firstParent || undefined,
        isRunning,
        isCompleted,
        computedResult,
        isInputStep,
        seqContainerName,
    };

    if (apiNode.type === 'WorkflowRun') {
        modelNode.estimatedDurationInMillis = apiNode.estimatedDurationInMillis;
        modelNode.isMultiBranch = true;
    }

    if (isInputStep) {
        modelNode.input = apiNode.input as APIInput;
    }

    return modelNode;
}

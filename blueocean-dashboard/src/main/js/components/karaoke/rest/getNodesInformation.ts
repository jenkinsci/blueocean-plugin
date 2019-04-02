import { capable } from '@jenkins-cd/blueocean-core-js';

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

// TODO: put these types somewhere better

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
    [name: string]: Link;

    self: Link;
}

interface Link {
    href: string;
}

type APIResult = 'SUCCESS' | 'UNSTABLE' | 'FAILURE' | 'NOT_BUILT' | 'UNKNOWN' | 'ABORTED';

type APIState = 'QUEUED' | 'RUNNING' | 'PAUSED' | 'SKIPPED' | 'NOT_BUILT' | 'FINISHED';

type APIInput = object; // TODO: Specify

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
    isParallel: boolean; // Set for the first (or only) stage in each parallel branch
    isSequential: boolean; // Set for second through last stages in a multi-stage parallel branch
    parent: string | null; // Containing stop stage node id for parallels, or previous top-level stage for top-level stages
    isRunning: boolean;
    isCompleted: boolean; // Running and completed are separate, because a job might be paused or waiting for resources or input
    computedResult: APIResult | APIState;

    // Promoted from APINode
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
    isMultiBranch?: boolean;
    isFocused?: boolean;
}

interface NodesInformation {
    isFinished: boolean;
    hasResultsForSteps: boolean;
    model: Array<ModelNode>;
    isError: boolean | null;
    runningNodes?: Array<string>;
    errorNodes?: Array<string>;
}

export function getNodesInformation(nodes: Array<APINode>): NodesInformation {
    // calculation of information about stages
    // nodes in Running state
    const runningNodes = nodes.filter(item => isRunningNode(item) && (!item.edges || item.edges.length < 2)).map(item => item.id);
    // nodes with error result
    const errorNodes = nodes.filter(item => item.result === RESULTS.FAILURE).map(item => item.id);
    const queuedNodes = nodes.filter(item => item.state === null && item.result === null).map(item => item.id);
    // nodes without information
    const hasResultsForSteps = nodes.filter(item => item.state === null && item.result === null).length !== nodes.length;
    // principal model mapper
    let wasFocused = false; // we only want one node to be focused if any
    let parent;
    // a job that is in queue would be marked as finished since
    // there will be no running nodes yet, that is why we check for that
    const finished = runningNodes.length === 0 && queuedNodes.length !== nodes.length;
    const error = !(errorNodes.length === 0);
    const model = nodes.map((item, index) => {
        const hasFailingNode = item.edges && item.edges.length >= 2 ? item.edges.filter(itemError => errorNodes.indexOf(itemError.id) > -1).length > 0 : false;
        const isFailingNode = errorNodes.indexOf(item.id) > -1;
        const isRunning = runningNodes.indexOf(item.id) > -1;

        const isParallel = item.type === 'PARALLEL';
        const isSequential = !!item.firstParent && item.firstParent !== parent;

        const logActions = item.actions ? item.actions.filter(action => capable(action, 'org.jenkinsci.plugins.workflow.actions.LogAction')) : [];
        const hasLogs = logActions.length > 0;
        const isCompleted = item.result !== 'UNKNOWN' && item.result !== null;
        const computedResult = (isCompleted ? item.result : item.state) || 'UNKNOWN';
        const isInputStep = !!item.input;
        const key = '' + index + isRunning + computedResult; // TODO: just use the node id
        const title = item.displayDescription ? item.displayName + ': ' + item.displayDescription : item.displayName;
        const modelItem: ModelNode = {
            actions: item.actions,
            _links: item._links,
            key: key,
            id: item.id,
            edges: item.edges,
            type: item.type,
            displayName: item.displayName,
            displayDescription: item.displayDescription,
            title: title || `runId: ${item.id}`,
            durationInMillis: item.durationInMillis || undefined,
            startTime: item.startTime || undefined,
            result: item.result,
            state: item.state,
            restartable: item.restartable,
            hasLogs,
            logUrl: hasLogs ? logActions[0]._links.self.href : undefined,
            isParallel,
            isSequential,
            parent,
            firstParent: item.firstParent || undefined,
            isRunning,
            isCompleted,
            computedResult,
            isInputStep,
        };
        // Update the parent whenever we encounter a top-level node
        if (!(isParallel || isSequential)) {
            parent = item.id;
        }
        if (item.type === 'WorkflowRun') {
            modelItem.estimatedDurationInMillis = item.estimatedDurationInMillis;
            modelItem.isMultiBranch = true;
        }
        if ((isRunning || (isFailingNode && !hasFailingNode && finished)) && !wasFocused) {
            wasFocused = true;
            modelItem.isFocused = true;
        }
        if (isInputStep) {
            modelItem.input = item.input as APIInput;
        }
        return modelItem;
    });
    // in case we have all null we will focus the first node since we assume that this would
    // be the next node to be started
    if (queuedNodes.length === nodes.length && !wasFocused && model[0]) {
        model[0].isFocused = true;
    }
    // creating the response object
    const information: NodesInformation = {
        isFinished: finished,
        hasResultsForSteps,
        model,
        isError: null,
    };
    // on not finished we return null and not a bool since we do not know the result yet
    if (!finished) {
        information.isError = null;
    } else {
        information.isError = error;
    }
    if (!finished) {
        information.runningNodes = runningNodes;
    } else if (error) {
        information.errorNodes = errorNodes;
    }
    return information;
}

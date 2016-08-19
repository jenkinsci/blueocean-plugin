import keymirror from 'keymirror';

export const RESULTS = keymirror({
    UNKNOWN: null,
    SUCCESS: null,
    FAILURE: null,
});

export const STATES = keymirror({
    RUNNING: null,
    FINISHED: null,
});

export const getNodesInformation = (nodes) => {

  // calculation of information about stages
  // nodes in Runing state
    const runningNodes = nodes
    .filter((item) => item.state === STATES.RUNNING && (!item.edges || item.edges.length < 2))
    .map((item) => item.id);
  // nodes with error result
    const errorNodes = nodes
    .filter((item) => item.result === RESULTS.FAILURE)
    .map((item) => item.id);
  // nodes without information
    const hasResultsForSteps = nodes
        .filter((item) => item.state === null && item.result === null).length !== nodes.length;
  // principal model mapper
    let wasFocused = false; // we only want one node to be focused if any
    const model = nodes.map((item, index) => {
        const hasFailingNode = item.edges ? item.edges
          .filter((itemError) => errorNodes.indexOf(itemError.id) > -1).length > 0 : false;
        const isFailingNode = errorNodes.indexOf(item.id) > -1;
        const isRunningNode = runningNodes.indexOf(item.id) > -1;

        // FIXME: TS I need to talk to cliffMeyers how we can refactor the following code to use capabilities
        // the problem I see ATM is that we would need to ask the c-API everytime for each action, whether this
        // action has the capability for logging
        const hasLogs = item.actions ? item.actions
                .filter(action => action._class === 'org.jenkinsci.plugins.workflow.support.actions.LogActionImpl').length > 0
            : false;
        const modelItem = {
            key: index,
            id: item.id,
            edges: item.edges,
            displayName: item.displayName,
            title: item.displayName || `runId: ${item.id}`,
            durationInMillis: item.durationInMillis,
            startTime: item.startTime,
            result: item.result,
            state: item.state,
            hasLogs,
        };
        if (item.type === 'WorkflowRun') {
            modelItem.estimatedDurationInMillis = item.estimatedDurationInMillis;
            modelItem.isMultiBranch = true;
        }
        if ((isRunningNode || (isFailingNode && !hasFailingNode)) && !wasFocused) {
            wasFocused = true;
            modelItem.isFocused = true;
        }
        return modelItem;
    });

    // FIXME: this assumaption is not 100% correct since a job that is in queue would be marked as finished since
    // there will be no running nodes yet!
    const finished = runningNodes.length === 0;
    const error = !(errorNodes.length === 0);

  // creating the response object
    const information = {
        isFinished: finished,
        hasResultsForSteps,
        model,
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
};

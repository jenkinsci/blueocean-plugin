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

export const getStagesInformation = (nodes) => {
    const information = {};

  // calculation of information about stages
  // nodes in Runing state
    const runningNodes = nodes
    .filter((item) => item.state === STATES.RUNNING)
    .map((item) => item.id);
  // nodes with error result
    const errorNodes = nodes
    .filter((item) => item.result === RESULTS.FAILURE)
    .map((item) => item.id);
  // principal model mapper
    let wasFocused = false; // we only want one node to be focused if any
    const model = nodes.map((item, index) => {
        const hasFailingNode = item.edges ? item.edges
          .filter((itemError) => errorNodes.indexOf(itemError.id) > -1).length > 0 : false;
        const isFailingNode = errorNodes.indexOf(item.id) > -1;
        const isRunningNode = runningNodes.indexOf(item.id) > -1;

        const modelItem = {
            key: index,
            id: item.id,
            title: item.displayName || `runId: ${item.id}`,
            durationInMillis: item.durationInMillis,
            startTime: item.startTime,
            result: item.result,
            state: item.state,
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

    const finished = runningNodes.length === 0;
    const error = !(errorNodes.length === 0);

  // creating the response object
    information.model = model;
    information.isFinished = finished;
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

import keymirror from 'keymirror';
import fetch from 'isomorphic-fetch';

import { State } from '../components/records';
import UrlConfig from '../config';
import { getNodesInformation } from '../util/logDisplayHelper';
import { calculateStepsBaseUrl, calculateLogUrl, calculateNodeBaseUrl } from '../util/UrlUtils';

/**
 * This function maps a queue item into a run instancce.
 *
 * We do this because the api returns us queued items as well
 * as runs and its easier to deal with them if they are modeled
 * as the same thing. If the raw data is needed if can be fetched
 * from _item.
 */
function _mapQueueToPsuedoRun(run) {
    if (run._class === 'io.jenkins.blueocean.service.embedded.rest.QueueItemImpl') {
        return {
            id: String(run.expectedBuildNumber),
            state: 'QUEUED',
            pipeline: run.pipeline,
            type: 'QueuedItem',
            result: 'UNKNOWN',
            job_run_queueId: run.id,
            enQueueTime: run.queuedTime,
            organization: run.organization,
            changeSet: [],
            _item: run,
        };
    }
    return run;
}

// main actin logic
export const ACTION_TYPES = keymirror({
    UPDATE_MESSAGES: null,
    CLEAR_PIPELINES_DATA: null,
    SET_PIPELINES_DATA: null,
    SET_PIPELINE: null,
    CLEAR_PIPELINE_DATA: null,
    SET_RUNS_DATA: null,
    SET_CURRENT_RUN_DATA: null,
    CLEAR_CURRENT_RUN_DATA: null,
    SET_BRANCHES_DATA: null,
    SET_CURRENT_BRANCHES_DATA: null,
    CLEAR_CURRENT_BRANCHES_DATA: null,
    SET_TEST_RESULTS: null,
    UPDATE_BRANCH_DATA: null,
    SET_STEPS: null,
    SET_NODE: null,
    SET_NODES: null,
    SET_LOGS: null,
    SET_CAPABILITIES: null,

});

export const actionHandlers = {
    [ACTION_TYPES.UPDATE_MESSAGES](state, { payload }): State {
        const messages = state.get('messages') || [];
        if (payload) {
            messages.push(payload);
        }
        return state.set('messages', messages);
    },
    [ACTION_TYPES.CLEAR_PIPELINES_DATA](state) {
        return state.set('pipelines', null);
    },
    [ACTION_TYPES.SET_PIPELINES_DATA](state, { payload }): State {
        return state.set('pipelines', payload);
    },
    [ACTION_TYPES.CLEAR_PIPELINE_DATA](state) {
        return state.set('pipeline', null);
    },
    [ACTION_TYPES.SET_PIPELINE](state, { id }): State {
        const pipelines = state.pipelines;
        if (!pipelines) {
            return state.set('pipeline', null);
        }
        // [].slice(0) returns a clone, we do need it for uniqueness
        const pipeline = pipelines.slice(0).filter(item => item.fullName === id);
        return state.set('pipeline', pipeline[0] ? pipeline[0] : null);
    },
    [ACTION_TYPES.CLEAR_CURRENT_RUN_DATA](state) {
        return state.set('currentRuns', null);
    },
    [ACTION_TYPES.SET_CURRENT_RUN_DATA](state, { payload }): State {
        return state.set('currentRuns', payload.map((run) => _mapQueueToPsuedoRun(run)));
    },
    [ACTION_TYPES.SET_NODE](state, { payload }): State {
        return state.set('node', { ...payload });
    },
    [ACTION_TYPES.SET_CAPABILITIES](state, { payload }): State {
        const caps = { ...state.caps } || {};
        for(const clzz of Object.keys(payload.map)) {
            const entry = payload.map[clzz];
            caps[clzz] = entry.classes;
        }
        return state.set('capabilities', caps);
    },
    [ACTION_TYPES.SET_NODES](state, { payload }): State {
        const nodes = { ...state.nodes } || {};
        nodes[payload.nodesBaseUrl] = payload;
        return state.set('nodes', nodes);
    },
    [ACTION_TYPES.SET_RUNS_DATA](state, { payload, id }): State {
        const runs = { ...state.runs } || {};

        runs[id] = payload.map(run => _mapQueueToPsuedoRun(run));
        return state.set('runs', runs);
    },
    [ACTION_TYPES.CLEAR_CURRENT_BRANCHES_DATA](state) {
        return state.set('currentBranches', null);
    },
    [ACTION_TYPES.SET_CURRENT_BRANCHES_DATA](state, { payload }): State {
        return state.set('currentBranches', payload);
    },
    [ACTION_TYPES.SET_BRANCHES_DATA](state, { payload, id }): State {
        const branches = { ...state.branches } || {};
        branches[id] = payload;
        return state.set('branches', branches);
    },
    [ACTION_TYPES.SET_TEST_RESULTS](state, { payload }): State {
        return state.set('testResults', payload === undefined ? {} : payload);
    },
    [ACTION_TYPES.SET_STEPS](state, { payload }): State {
        const steps = { ...state.steps } || {};
        steps[payload.nodesBaseUrl] = payload;
        return state.set('steps', steps);
    },
    [ACTION_TYPES.SET_LOGS](state, { payload }): State {
        const logs = { ...state.logs } || {};
        logs[payload.logUrl] = payload;

        return state.set('logs', logs);
    },

    [ACTION_TYPES.UPDATE_BRANCH_DATA](state, { payload, id }): State {
        const branches = state.get('branches') || {};
        const jobBranches = branches[id];

        // store the new branch data for the single branch
        // then update all branch data in the store
        const newBranches = jobBranches.map(branch =>
            branch.name === payload.name ?
                payload : branch
        );

        branches[id] = newBranches;
        return state
            .set('branches', branches)
            .set('currentBranches', newBranches);
    },
};

// fetch helper
const fetchOptions = { credentials: 'same-origin' };
function checkStatus(response) {
    if (response.status >= 300 || response.status < 200) {
        const error = new Error(response.statusText);
        error.response = response;
        throw error;
    }
    return response;
}

function parseJSON(response) {
    return response.json();
}

function parseMoreDataHeader(response) {
    let newStart = null;
    /*
     * If X-More-Data is true, then client should repeat the request after some delay.
     * In the repeated request it should use X-TEXT-SIZE header value with start query parameter.
     */
    if (response.headers.get('X-More-Data')) {
        /*
         * X-TEXT-SIZE is the byte offset of the raw log file client should use in the next request
         * as value of start query parameter.
         */
        newStart = response.headers.get('X-TEXT-SIZE');
    }
    const payload = { response, newStart };
    return payload;
}
/**
 * Fetch JSON data.
 * <p>
 * Utility function that can be mocked for testing.
 *
 * @param url The URL to fetch from.
 * @param onSuccess o
 * @param onError
 */
exports.fetchJson = function fetchJson(url, onSuccess, onError) {
    return fetch(url, fetchOptions)
        .then(checkStatus)
        .then(parseJSON)
        .then(onSuccess)
        .catch((error) => {
            if (onError) {
                onError(error);
            } else {
                console.error(error); // eslint-disable-line no-console
            }
        });
};

/**
 * Fetch TXT/log data and inject a start parameter to indicate that a refetch is needed
 * <p>
 * Utility function that can be mocked for testing.
 *
 * @param url The URL to fetch from.
 * @param start query parameter tells API to send log starting from this offset in the log file.
 * @param onSuccess Main callback to run specific callback code
 * @param onError Error callback
 */

exports.fetchLogsInjectStart = function fetchLogsInjectStart(url, start, onSuccess, onError) {
    let refetchUrl;
    if (start === null) {
        refetchUrl = url;
    } else {
        refetchUrl = `${url}?start=${start}`;
    }
    return fetch(refetchUrl, fetchOptions)
        .then(checkStatus)
        .then(parseMoreDataHeader)
        .then(onSuccess)
        .catch((error) => {
            if (onError) {
                onError(error);
            } else {
                console.error(error); // eslint-disable-line no-console
            }
        });
};
/**
 * Clone a JSON object/array instance.
 * <p>
 * This needs to be done for redux. See
 * http://redux.js.org//docs/recipes/UsingObjectSpreadOperator.html.
 * <p>
 * TODO: Maybe use object/array spread operator.
 * I didn't try because I was not sure if they could be used for this as it seems
 * like they only perform a shallow clone.
 *
 * @param json The JSON object/array to be cloned.
 */
function clone(json) {
    return JSON.parse(JSON.stringify(json));
}

// FIXME: Ignoring isFetching for now
export const actions = {
    clearPipelinesData: () => ({ type: ACTION_TYPES.CLEAR_PIPELINES_DATA }),
    clearPipelineData() {
        return (dispatch) => dispatch({ type: ACTION_TYPES.CLEAR_PIPELINE_DATA });
    },

    /**
     * Unconditionally fetch and update the pipelines list.
     * @param config Application configuration.
     * @param organizationName (optional)
     */
    // eslint-disable-next-line no-unused-vars
    fetchPipelines(config, organizationName) {
        return (dispatch) => {
            const baseUrl = config.getAppURLBase();
            // TODO: update this code to call /search with organizationName once JENKINS-36273 is ready
            const url = `${baseUrl}/rest/search/?q=type:pipeline;excludedFromFlattening:jenkins.branch.MultiBranchProject`;

            return dispatch(actions.generateData(
                url,
                ACTION_TYPES.SET_PIPELINES_DATA
            ));
        };
    },

    /**
     * Fetch and update the pipelines list if the store doesn't already have
     * a list of the pipelines.
     * @param config Application configuration.
     * @param organizationName (optional)
     */
    // eslint-disable-next-line no-unused-vars
    fetchPipelinesIfNeeded(config, organizationName) {
        return (dispatch, getState) => {
            const pipelines = getState().adminStore.pipelines;
            const baseUrl = config.getAppURLBase();
            // TODO: update this code to call /search with organizationName once JENKINS-36273 is ready
            const url = `${baseUrl}/rest/search/?q=type:pipeline;excludedFromFlattening:jenkins.branch.MultiBranchProject`;

            if (!pipelines) {
                return dispatch(actions.generateData(
                    url,
                    ACTION_TYPES.SET_PIPELINES_DATA
                ));
            }
            return pipelines;
        };
    },

    setPipeline(config) {
        return (dispatch, getState) => {
            dispatch({ type: ACTION_TYPES.CLEAR_PIPELINE_DATA });
            const pipelines = getState().adminStore.pipelines;

            if (!pipelines) {
                return dispatch(actions.fetchPipelinesIfNeeded(config))
                    .then(() => dispatch({ id: config.pipeline, type: ACTION_TYPES.SET_PIPELINE }));
            }
            return dispatch({ id: config.pipeline, type: ACTION_TYPES.SET_PIPELINE });
        };
    },

    processJobQueuedEvent(event) {
        return (dispatch, getState) => {
            const runsByJobName = getState().adminStore.runs || {};
            const eventJobRuns = runsByJobName[event.blueocean_job_pipeline_name];

            // Only interested in the event if we have already loaded the runs for that job.
            if (eventJobRuns && event.job_run_queueId) {
                for (let i = 0; i < eventJobRuns.length; i++) {
                    const run = eventJobRuns[i];
                    if (event.job_ismultibranch
                        && event.blueocean_job_branch_name !== run.pipeline) {
                        // Not the same branch. Yes, run.pipeline actually contains
                        // the branch name i.e. naming seems a bit confusing.
                        continue;
                    }
                    if (run.job_run_queueId === event.job_run_queueId) {
                        // We already have a "dummy" record for this queued job
                        // run. No need to create another i.e. ignore this event.
                        return;
                    }
                }

                // Create a new "dummy" entry in the runs list for the
                // run that's been queued.
                const newRun = {};

                // We keep the queueId so we can cross reference it with the actual
                // run once it has been started.
                newRun.job_run_queueId = event.job_run_queueId;
                if (event.job_ismultibranch) {
                    newRun.pipeline = event.blueocean_job_branch_name;
                } else {
                    newRun.pipeline = event.blueocean_job_pipeline_name;
                }
                newRun.state = 'QUEUED';
                newRun.result = 'UNKNOWN';

                const newRuns = clone([newRun, ...eventJobRuns]);

                if (event.blueocean_is_for_current_job) {
                    // set current runs since we are ATM looking at it
                    dispatch({ payload: newRuns, type: ACTION_TYPES.SET_CURRENT_RUN_DATA });
                }
                dispatch({
                    payload: newRuns,
                    id: event.blueocean_job_pipeline_name,
                    type: ACTION_TYPES.SET_RUNS_DATA,
                });
            }
        };
    },

    updateRunState(event, config, updateByQueueId) {
        return (dispatch, getState) => {
            let storeData;

            // Go to the redux store and get a fresh copy of the run data associated
            // with the event. We need to be able to do this because we do an async
            // fetch and so need to be able refresh the data used when processing
            // the event i.e. we need to get from the store more than once - before
            // and after the fetch. Need to get it after the fetch because things
            // may have changed state.
            function getFromStore() {
                const runsByJobName = getState().adminStore.runs || {};
                const eventJobRuns = runsByJobName[event.blueocean_job_pipeline_name];
                let newStoreData = undefined;

                // Only interested in the event if we have already loaded the runs for that job.
                if (eventJobRuns) {
                    newStoreData = {};
                    newStoreData.eventJobRuns = eventJobRuns;

                    for (let i = 0; i < eventJobRuns.length; i++) {
                        const run = eventJobRuns[i];
                        if (event.job_ismultibranch
                            && event.blueocean_job_branch_name !== run.pipeline) {
                            // Not the same branch. Yes, run.pipeline actually contains
                            // the branch name.
                            continue;
                        }
                        if (updateByQueueId) {
                            // We use the queueId to locate the right "dummy" run entry that
                            // needs updating. The "dummy" run entry was created in
                            // processJobQueuedEvent().
                            if (run.job_run_queueId === event.job_run_queueId) {
                                newStoreData.runIndex = i;
                                break;
                            }
                        } else {
                            if (run.id === event.jenkins_object_id) {
                                newStoreData.runIndex = i;
                                break;
                            }
                        }
                    }
                }

                return newStoreData;
            }

            // Get the event related data from the
            // redux store.
            storeData = getFromStore();

            // Only interested in the event if we have already loaded the runs for that job.
            if (storeData) {
                const updateRunData = function updateRunData(runData, skipStoreDataRefresh) {
                    const newRunData = Object.assign({}, runData);
                    let newRuns;

                    // Only need to update the storeData if something async
                    // happened i.e. giving an opportunity for the current
                    // copy of the start data to become "stale".
                    if (!skipStoreDataRefresh) {
                        storeData = getFromStore();
                    }

                    // In theory, the following code should not be needed as the
                    // call to the REST API should return run data with a state
                    // that's at least as up-to-date as the state received in
                    // event that triggered this. However, that's not what has
                    // been e.g. we've seen run start events coming in, triggering
                    // a call of the REST API, but the run state coming back for
                    // that same run may still be "QUEUED".
                    // Note, if you put a breakpoint in and wait for a second before
                    // allowing the REST API call, then you get the right state.
                    // So, it seems like the RunListener event is being fired
                    // in Jenkins core before the state is properly persisted.
                    if (event.jenkins_event === 'job_run_ended') {
                        newRunData.state = 'FINISHED';
                    } else {
                        newRunData.state = 'RUNNING';
                    }

                    if (storeData.runIndex !== undefined) {
                        newRuns = clone(storeData.eventJobRuns);
                        newRuns[storeData.runIndex] = newRunData;
                    } else {
                        newRuns = clone([newRunData, ...storeData.eventJobRuns]);
                    }

                    if (event.blueocean_is_for_current_job) {
                        // set current runs since we are ATM looking at it
                        dispatch({ payload: newRuns, type: ACTION_TYPES.SET_CURRENT_RUN_DATA });
                    }
                    dispatch({
                        payload: newRuns,
                        id: event.blueocean_job_pipeline_name,
                        type: ACTION_TYPES.SET_RUNS_DATA,
                    });
                };

                const runUrl = `${config.getAppURLBase()}${event.blueocean_job_rest_url}/runs/${event.jenkins_object_id}`;

                // The event tells us that the run state has changed, but does not give all
                // run related data (times, commit Ids etc). So, lets go get that data from
                // REST API and present a consistent picture of the run state to the user.
                exports.fetchJson(runUrl, updateRunData, (error) => {
                    let runData;

                    // Getting the actual state of the run failed. Lets log
                    // the failure and update the state manually as best we can.

                    // eslint-disable-next-line no-console
                    console.warn(`Error getting run data from REST endpoint: ${runUrl}`);
                    // eslint-disable-next-line no-console
                    console.warn(error);

                    // We're after coming out of an async operation (the fetch).
                    // In that case, we better refresh the copy of the storeData
                    // that we have in case things changed while we were doing the
                    // fetch.
                    storeData = getFromStore();

                    if (storeData.runIndex !== undefined) {
                        runData = storeData.eventJobRuns[storeData.runIndex];
                    } else {
                        runData = {};
                        runData.job_run_queueId = event.job_run_queueId;
                        if (event.job_ismultibranch) {
                            runData.pipeline = event.blueocean_job_branch_name;
                        } else {
                            runData.pipeline = event.blueocean_job_pipeline_name;
                        }
                    }

                    if (event.jenkins_event === 'job_run_ended') {
                        runData.state = 'FINISHED';
                    } else {
                        runData.state = 'RUNNING';
                    }
                    runData.id = event.jenkins_object_id;
                    runData.result = event.job_run_status;

                    // Update the run data. We do not need updateRunData to refresh the
                    // storeData again because we already just did it at the start of
                    // this function call.
                    updateRunData(runData, false);
                });
            }
        };
    },

    updateBranchState(event, config) {
        return (dispatch, getState) => {
            // if the job event is multibranch, refetch the corresponding branch
            // from the REST API and update our stores
            if (event.job_ismultibranch) {
                const branches = getState().adminStore.branches || {};
                const jobs = branches[event.blueocean_job_pipeline_name] || [];
                const branch = jobs.find(job => job.name === event.blueocean_job_branch_name);

                if (!branch) {
                    return;
                }

                const url = `${config.getAppURLBase()}${event.blueocean_job_rest_url}`;

                const processBranchData = function processBranchData(branchData) {
                    const { latestRun } = branchData;

                    // same issue as in 'updateRunData'; see comment above
                    if (event.jenkins_event === 'job_run_ended') {
                        latestRun.state = 'FINISHED';
                    } else {
                        latestRun.state = 'RUNNING';
                    }

                    // apply the new data to the store
                    dispatch({
                        payload: branchData,
                        id: event.blueocean_job_pipeline_name,
                        type: ACTION_TYPES.UPDATE_BRANCH_DATA,
                    });
                };

                exports.fetchJson(url, processBranchData, (error) => {
                    console.log(error); // eslint-disable-line no-console
                });
            }
        };
    },

    updateBranchList(event, config) {
        return (dispatch, getState) => {
            if (event.job_ismultibranch) {
                const multibranchPipelines = getState().adminStore.branches || {};
                const pipelineName = event.blueocean_job_pipeline_name;

                // We're only interested in this event if we're already managing branch state
                // associated with this multi-branch job.
                if (!multibranchPipelines[pipelineName]) {
                    return;
                }

                // Fetch/refetch the latest set of branches for the pipeline.
                const url = `${config.getAppURLBase()}/rest/organizations/${event.jenkins_org}` +
                    `/pipelines/${pipelineName}/branches`;
                exports.fetchJson(url, (latestPipelineBranches) => {
                    if (event.blueocean_is_for_current_job) {
                        dispatch({
                            id: pipelineName,
                            payload: latestPipelineBranches,
                            type: ACTION_TYPES.SET_CURRENT_BRANCHES_DATA,
                        });
                    }
                    dispatch({
                        id: pipelineName,
                        payload: latestPipelineBranches,
                        type: ACTION_TYPES.SET_BRANCHES_DATA,
                    });
                });
            }
        };
    },

    fetchRunsIfNeeded(config) {
        return (dispatch) => {
            const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
                `/pipelines/${config.pipeline}/activities/`;
            return dispatch(actions.fetchIfNeeded({
                url: baseUrl,
                id: config.pipeline,
                type: 'runs',
            }, {
                current: ACTION_TYPES.SET_CURRENT_RUN_DATA,
                general: ACTION_TYPES.SET_RUNS_DATA,
                clear: ACTION_TYPES.CLEAR_CURRENT_RUN_DATA,
            }));
        };
    },

    fetchBranchesIfNeeded(config) {
        return (dispatch) => {
            const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
                `/pipelines/${config.pipeline}/branches`;
            return dispatch(actions.fetchIfNeeded({
                url: baseUrl,
                id: config.pipeline,
                type: 'branches',
            }, {
                current: ACTION_TYPES.SET_CURRENT_BRANCHES_DATA,
                general: ACTION_TYPES.SET_BRANCHES_DATA,
                clear: ACTION_TYPES.CLEAR_CURRENT_BRANCHES_DATA,
            }));
        };
    },

    /**
     * Check the redux store for data and fetch from the REST API if needed.
     * @param general TODO: what's this and what's in it?
     * @param types TODO: what's this and what's in it?
     * @returns {Function}
     */
    fetchIfNeeded(general, types) {
        return (dispatch, getState) => {
            const data = getState().adminStore[general.type];
            dispatch({ type: types.clear });

            const id = general.id;

            if (!data || !data[id]) {
                return fetch(general.url, fetchOptions)
                    .then(checkStatus)
                    .then(parseJSON)
                    .then(json => {
                        // TODO: Why call dispatch twice here?
                        dispatch({
                            id,
                            payload: json,
                            type: types.current,
                        });
                        return dispatch({
                            id,
                            payload: json,
                            type: types.general,
                        });
                    })
                    .catch((error) => {
                        console.error(error); // eslint-disable-line no-console
                        dispatch({
                            payload: { type: 'ERROR', message: `${error.stack}` },
                            type: ACTION_TYPES.UPDATE_MESSAGES,
                        });
                    });
            } else if (data && data[id]) {
                dispatch({
                    id,
                    payload: data[id],
                    type: types.current,
                });
            }
            return null;
        };
    },

    generateData(url, actionType, optional) {
        return (dispatch) => fetch(url, fetchOptions)
            .then(checkStatus)
            .then(parseJSON)
            .then(json => dispatch({
                ...optional,
                type: actionType,
                payload: json,
            }))
            .catch((error) => {
                console.error(error); // eslint-disable-line no-console
                dispatch({
                    payload: { type: 'ERROR', message: `${error.stack}` },
                    type: ACTION_TYPES.UPDATE_MESSAGES,
                });
                // call again with no payload so actions handle missing data
                dispatch({
                    ...optional,
                    type: actionType,
                });
            });
    },
    /*
     For the detail view we need to fetch the different nodes of
     a run in case we do not have specific node, to
     determine which one we have to show in the detail view.
     We later store them with the key: nodesBaseUrl
     so we only fetch them once.
     */
    fetchNodes(config) {
        return (dispatch, getState) => {
            const data = getState().adminStore.nodes;
            const nodesBaseUrl = calculateNodeBaseUrl(config);

            function getNodeAndSteps(information) {
                let nodeModel;
                let node;
                if (!config.node) {
                    const focused = information.model.filter((item) => item.isFocused)[0];
                    if (focused) {
                        nodeModel = focused;
                    } else {
                        nodeModel = (information.model[information.model.length - 1]);
                    }
                    node = nodeModel ? nodeModel.id : null;
                } else {
                    nodeModel = information.model.filter((item) => item.id === config.node)[0];
                    node = config.node;
                }

                dispatch({
                    type: ACTION_TYPES.SET_NODE,
                    payload: nodeModel,
                });
                const mergedConfig = { ...config, node };
                return dispatch(actions.fetchSteps(mergedConfig));
            }

            if (!data || !data[nodesBaseUrl] || config.refetch) {
                return exports.fetchJson(
                    nodesBaseUrl,
                    (json) => {
                        const information = getNodesInformation(json);
                        information.nodesBaseUrl = nodesBaseUrl;
                        dispatch({
                            type: ACTION_TYPES.SET_NODES,
                            payload: information,
                        });

                        return getNodeAndSteps(information);
                    },
                    (error) => console.error('error', error) // eslint-disable-line no-console
                );
            }
            return getNodeAndSteps(data[nodesBaseUrl]);
        };
    },

    setNode(config) {
        return (dispatch, getState) => {
            const data = getState().adminStore.nodes;
            const nodesBaseUrl = calculateNodeBaseUrl(config);
            if (!data || !data[nodesBaseUrl] || config.refetch) {
                return actions.fetchNodes(config);
            }
            const node = data[nodesBaseUrl].model.filter((item) => item.id === config.node)[0];
            return dispatch({
                type: ACTION_TYPES.SET_NODE,
                payload: node,
            });
        };
    },

    cleanNodePointer() {
        return (dispatch) => dispatch({
            type: ACTION_TYPES.SET_NODE,
            payload: null,
        });
    },
    /*
     For the detail view we need to fetch the different steps of a nodes.
     We later store them with the key: nodesBaseUrl
     so we only fetch them once.
     */
    fetchSteps(config) {
        return (dispatch, getState) => {
            const data = getState().adminStore.steps;
            const stepBaseUrl = calculateStepsBaseUrl(config);
            if (!data || !data[stepBaseUrl] || config.refetch) {
                return exports.fetchJson(
                  stepBaseUrl,
                  (json) => {
                      const information = getNodesInformation(json);
                      information.nodesBaseUrl = stepBaseUrl;
                      return dispatch({
                          type: ACTION_TYPES.SET_STEPS,
                          payload: information,
                      });
                  },
                  (error) => console.error('error', error) // eslint-disable-line no-console
                );
            }
            return null;
        };
    },
    /* l
     Get a specific log for a node, fetch it only if needed.
     key for cache: logUrl = calculateLogUrl
     */
    fetchLog(config) {
        return (dispatch, getState) => {
            const data = getState().adminStore.logs;
            const logUrl = calculateLogUrl(config);
            if (
                config.fetchAll ||
                !data || !data[logUrl] ||
                config.newStart > 0 ||
                (data && data[logUrl] && data[logUrl].newStart > 0 || !data[logUrl].logArray)
            ) {
                return exports.fetchLogsInjectStart(
                    logUrl,
                    config.newStart || null,
                    response => response.response.text()
                        .then(text => {
                            // By default only last 150 KB log data is returned in the response.
                            const maxLength = 150000;
                            const contentLength = Number(response.response.headers.get('X-Text-Size'));
                            // set flag that there are more logs then we deliver
                            let hasMore = contentLength > maxLength;
                            // when we came from ?start=0, hasMore has to be false since there is no more
                            // console.log(config.fetchAll, 'inner')
                            if (config.fetchAll) {
                                hasMore = false;
                            }
                            const { newStart } = response;
                            const payload = {
                                logUrl,
                                newStart,
                                hasMore,
                            };
                            if (text && !!text.trim()) {
                                payload.logArray = text.trim().split('\n');
                            }
                            return dispatch({
                                payload,
                                type: ACTION_TYPES.SET_LOGS,
                            });
                        }),
                    (error) => console.error('error', error) // eslint-disable-line no-console
                );
            }
            return null;
        };
    },

    fetchTestResults(run) {
        return (dispatch) => {
            const baseUrl = UrlConfig.getJenkinsRootURL();
            const url = `${baseUrl}${run._links.self.href}testReport/result`;

            return dispatch(actions.generateData(
                url,
                ACTION_TYPES.SET_TEST_RESULTS
            ));
        };
    },

    fetchCapabilitiesIfNeeded(_class) {
        return (dispatch, getState) => {
            if(!_class) return;
            const caps = getState().adminStore.capabilities;
            const baseUrl = UrlConfig.jenkinsRootURL;
            const url = `${baseUrl}blue/rest/classes/?q=${_class}`;

            if(caps && caps[_class]) {
                return null;
            } else {
                return dispatch(actions.generateData(
                    url,
                    ACTION_TYPES.SET_CAPABILITIES
                ))
            }

        }
    },

    resetTestDetails() {
        return (dispatch) =>
            dispatch({
                type: ACTION_TYPES.SET_TEST_RESULTS,
                payload: null,
            });
    },
};

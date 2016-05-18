import keymirror from 'keymirror';

import fetch from 'isomorphic-fetch';
import { State } from '../components/records';

export const ACTION_TYPES = keymirror({
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
});

export const actionHandlers = {
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
        const pipelines = state.get('pipelines');
        if (!pipelines) {
            return state.set('pipeline', null);
        }
        const pipeline = pipelines.filter(item => item.name === id);
        return state.set('pipeline', pipeline[0] ? pipeline[0] : null);
    },
    [ACTION_TYPES.CLEAR_CURRENT_RUN_DATA](state) {
        return state.set('currentRuns', null);
    },
    [ACTION_TYPES.SET_CURRENT_RUN_DATA](state, { payload }): State {
        return state.set('currentRuns', payload);
    },
    [ACTION_TYPES.SET_RUNS_DATA](state, { payload, id }): State {
        const runs = state.get('runs') || {};
        runs[id] = payload;
        return state.set('runs', runs);
    },
    [ACTION_TYPES.CLEAR_CURRENT_BRANCHES_DATA](state) {
        return state.set('currentBranches', null);
    },
    [ACTION_TYPES.SET_CURRENT_BRANCHES_DATA](state, { payload }): State {
        return state.set('currentBranches', payload);
    },
    [ACTION_TYPES.SET_BRANCHES_DATA](state, { payload, id }): State {
        const branches = state.get('branches') || {};
        branches[id] = payload;
        return state.set('branches', branches);
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

    fetchPipelinesIfNeeded(config) {
        return (dispatch, getState) => {
            const pipelines = getState().adminStore.pipelines;
            const url = `${config.getAppURLBase()}` +
                '/rest/organizations/jenkins/pipelines/';
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
            const eventJobRuns = runsByJobName[event.blueocean_job_name];

            // Only interested in the event if we have already loaded the runs for that job.
            if (eventJobRuns && event.job_run_queueId) {
                for (let i = 0; i < eventJobRuns.length; i++) {
                    const run = eventJobRuns[i];
                    if (event.blueocean_is_multi_branch
                        && event.blueocean_branch_name !== run.pipeline) {
                        // Not the same branch. Yes, run.pipeline actually contains
                        // the branch name.
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
                if (event.blueocean_is_multi_branch) {
                    newRun.pipeline = event.blueocean_branch_name;
                } else {
                    newRun.pipeline = event.blueocean_job_name;
                }
                newRun.state = 'QUEUED';
                newRun.result = 'UNKNOWN';

                const newRuns = clone([newRun, ...eventJobRuns]);

                if (event.blueocean_is_for_current_job) {
                    // set current runs since we are ATM looking at it
                    dispatch({ payload: newRuns, type: ACTION_TYPES.SET_CURRENT_RUN_DATA });
                }
                dispatch({ payload: newRuns,
                    id: event.blueocean_job_name,
                    type: ACTION_TYPES.SET_RUNS_DATA });
            }
        };
    },

    updateRunState(event, config, updateByQueueId) {
        return (dispatch, getState) => {
            let storeData;

            function getFromStore() {
                const runsByJobName = getState().adminStore.runs || {};
                const eventJobRuns = runsByJobName[event.blueocean_job_name];
                let newStoreData = undefined;

                // Only interested in the event if we have already loaded the runs for that job.
                if (eventJobRuns) {
                    newStoreData = {};
                    newStoreData.eventJobRuns = eventJobRuns;

                    for (let i = 0; i < eventJobRuns.length; i++) {
                        const run = eventJobRuns[i];
                        if (event.blueocean_is_multi_branch
                            && event.blueocean_branch_name !== run.pipeline) {
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
                let runUrl;

                const updateRunData = function (runData, skipStoreDataRefresh) {
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
                    dispatch({ payload: newRuns,
                        id: event.blueocean_job_name,
                        type: ACTION_TYPES.SET_RUNS_DATA });
                };

                if (event.blueocean_is_multi_branch) {
                    // TODO: find out how to get a 'master' branch run.
                    // Doesn't work using 'master'??
                    runUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
                        `/pipelines/${event.blueocean_job_name}` +
                        `/branches/${event.blueocean_branch_name}/runs/${event.jenkins_object_id}`;
                } else {
                    runUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
                        `/pipelines/${event.blueocean_job_name}/runs/${event.jenkins_object_id}`;
                }

                // The event tells us that the run state has changed, but does not give all
                // run related date (times, commit Ids etc). So, lets go get that data from
                // REST API and present a consistent picture of the run state to the user.
                fetch(runUrl, fetchOptions)
                    .then(checkStatus)
                    .then(parseJSON)
                    .then(updateRunData)
                    .catch((error) => {
                        let runData;

                        // Getting the actual state of the run failed. Lets log
                        // the failure and update the state manually as best we can.

                        console.warn(`Error getting run data from REST endpoint: ${runUrl}`);
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
                            if (event.blueocean_is_multi_branch) {
                                runData.pipeline = event.blueocean_branch_name;
                            } else {
                                runData.pipeline = event.blueocean_job_name;
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

    fetchRunsIfNeeded(config) {
        return (dispatch) => {
            const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
            `/pipelines/${config.pipeline}/runs/`;
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
                    .catch(() => dispatch({
                        id,
                        payload: [],
                        type: types.current,
                    })
                    );
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
            .catch(() => dispatch({
                ...optional,
                payload: null,
                type: actionType,
            }));
    },
};

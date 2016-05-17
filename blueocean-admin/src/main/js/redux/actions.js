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

function getActivePipelineName(appState) {
    const currentJobRuns = appState.get('currentRuns');
    if (currentJobRuns && currentJobRuns.length > 0) {
        // Look inside the 1st run in currentJobRuns and use the pipeline
        // name on that to decide whether or not the currently displayed runs
        // need to be updated. Update if the pipeline name is the same as
        // the job_name on the event.
        return currentJobRuns[0].pipeline;
    }
    return undefined;
}

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
            const eventJobRuns = runsByJobName[event.blueocean_pipeline_name];

            // Only interested in the event if we have already loaded the runs for that job.
            if (eventJobRuns) {
                const currentPipelineName = getActivePipelineName(getState().adminStore);
                // Create a new "dummy" entry in the runs list for the
                // run that's been queued.
                const newRun = {};

                // We keep the queueId so we can cross reference it with the actual
                // run once it has been started.
                newRun.job_run_queueId = event.job_run_queueId;
                newRun.pipeline = event.blueocean_pipeline_name;
                newRun.state = 'QUEUED';
                newRun.result = 'UNKNOWN';

                const newRuns = clone([newRun, ...eventJobRuns]);

                if (currentPipelineName === event.blueocean_pipeline_name) {
                    // set current runs since we are ATM looking at it
                    dispatch({ payload: newRuns, type: ACTION_TYPES.SET_CURRENT_RUN_DATA });
                }
                return dispatch({ payload: newRuns,
                    id: event.blueocean_pipeline_name,
                    type: ACTION_TYPES.SET_RUNS_DATA });
            }
            return null;
        };
    },

    updateRunState(event, config, updateByQueueId) {
        return (dispatch, getState) => {
            const runsByJobName = getState().adminStore.runs || {};
            const eventJobRuns = runsByJobName[event.blueocean_pipeline_name];

            // Only interested in the event if we have already loaded the runs for that job.
            if (eventJobRuns) {
                const runUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
                    `/pipelines/${event.blueocean_pipeline_name}/runs/${event.jenkins_object_id}`;
                fetch(runUrl, fetchOptions)
                    .then(checkStatus)
                    .then(parseJSON)
                    .then(theRun => {
                        const currentPipelineName = getActivePipelineName(getState().adminStore);
                        let runIndex;
                        let newRuns;

                        for (let i = 0; i < eventJobRuns.length; i++) {
                            const run = eventJobRuns[i];
                            if (updateByQueueId) {
                                // We use the queueId to locate the right "dummy" run entry that
                                // needs updating. The "dummy" run entry was created in
                                // processJobQueuedEvent().
                                if (run.job_run_queueId === event.job_run_queueId) {
                                    runIndex = i;
                                    break;
                                }
                            } else {
                                if (run.id === event.jenkins_object_id) {
                                    runIndex = i;
                                    break;
                                }
                            }
                        }

                        if (runIndex !== undefined) {
                            newRuns = clone(eventJobRuns);
                            newRuns[runIndex] = theRun;
                        } else {
                            newRuns = clone([theRun, ...eventJobRuns]);
                        }

                        if (currentPipelineName === event.blueocean_pipeline_name) {
                            // set current runs since we are ATM looking at it
                            dispatch({ payload: newRuns, type: ACTION_TYPES.SET_CURRENT_RUN_DATA });
                        }
                        dispatch({ payload: newRuns,
                            id: event.blueocean_pipeline_name,
                            type: ACTION_TYPES.SET_RUNS_DATA });
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

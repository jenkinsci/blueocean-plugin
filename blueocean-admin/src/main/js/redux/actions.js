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

    fetchRunsIfNeeded(config) {
        return (dispatch) => {
            const baseUrl = `${config.getAppURLBase()}/rest/organizations/jenkins` +
            `/pipelines/${config.pipeline}/runs`;
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

    fetchIfNeeded(general, types) {
        return (dispatch, getState) => {
            const data = getState().adminStore[general.type];
            dispatch({ type: types.clear });

            const id = general.id;

            if (!data || !data[id]) {
                return fetch(general.url)
                    .then(response => response.json())
                    .then(json => {
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
        return (dispatch) => fetch(url)
            .then(response => response.json())
            .then(json => dispatch({
                ...optional,
                type: actionType,
                payload: json,
            }));
    },
};

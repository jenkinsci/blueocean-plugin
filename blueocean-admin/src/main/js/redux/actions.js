import keymirror from 'keymirror';

import fetch from 'isomorphic-fetch';
import { State } from './reduxState';

// Actions -> comes from a module
export const ACTION_TYPES = keymirror({
    CLEAR_PIPELINES_DATA: null,
    SET_PIPELINES_DATA: null,
    SET_RUNS_DATA: null,
    GET_PIPELINE: null,
});

export const actionHandlers = {
    [ACTION_TYPES.CLEAR_PIPELINES_DATA](state) {
        return state.set('pipelines', null);
    },
    [ACTION_TYPES.SET_PIPELINES_DATA](state, { payload }): State {
        return state.set('pipelines', payload);
    },
    [ACTION_TYPES.SET_RUNS_DATA](state, { payload, id }): State {
        console.log('xxxxxx', payload, id);
        const runs = state.get('runs');
        runs[id] = payload;
        return state.set('runs', runs);
    },
    [ACTION_TYPES.SET_CURRENT_RUN_DATA](state, { payload }): State {
        return state.set('currentRuns', payload);
    },
    [ACTION_TYPES.GET_PIPELINE](state, { id }): State {
        const pipeline = state.get('pipelines').filter(item => item.name === id);
        return state.set('pipeline', pipeline[0] ? pipeline[0] : null);
    },
};

export const actions = {
    clearPipelinesData: () => ({ type: ACTION_TYPES.CLEAR_PIPELINES_DATA }),

    fetchPipelinesIfNeeded(url) {
        return (dispatch, getState) => {
            // FIXME: Ignoring isFetching for now
            const pipelines = getState().adminStore.pipelines;
            if (!pipelines) {
                return dispatch(actions.generateData(
                    url,
                    ACTION_TYPES.SET_PIPELINES_DATA
                ));
            }
            return null;
        };
    },

    fetchRunsIfNeeded(url, id) {
        return (dispatch, getState) => {
            // FIXME: Ignoring isFetching for now
            const runs = getState().adminStore.runs;
            if (!runs) {
                return dispatch(actions.generateData(
                    url, ACTION_TYPES.SET_RUNS_DATA));
            }
            return null;
        };
    },

    getPipeline(id) {
        return (dispatch) => dispatch({
            id,
            type: ACTION_TYPES.GET_PIPELINE,
        });
    },

    generateData(url, actionType, optional) {
        return (dispatch, getState) => {
            console.log(url, actionType, getState().adminStore.pipelines, {
                optional,
                type: actionType,
            });

            return fetch(url)
                    .then(response => response.json())
                    .then(json => dispatch({
                        ...optional,
                        type: actionType,
                        payload: json,
                    }));
        };
    },
};

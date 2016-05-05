import { createSelector } from 'reselect';
import { actionHandlers } from './actions';
import { State } from './reduxState';

export const adminStore = state => state.adminStore;
export const pipelines = createSelector([adminStore], store => store.pipelines);
export const pipeline = createSelector([adminStore], store => store.pipeline);
export const runs = createSelector([adminStore], store => store.runs);
export const currentRuns = createSelector([adminStore], store => store.currentRuns);

// reducer
export function reducer(state = new State(), action:Object):State {
    const { type } = action;
    if (type in actionHandlers) {
        return actionHandlers[type](state, action);
    }
    return state;
}

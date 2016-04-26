import { createSelector } from 'reselect';
import { actionHandlers } from './actions';
import { State } from './reduxState';

export const adminStore = state => state.adminStore;
export const pipelines = createSelector([adminStore], adminStore => adminStore.pipelines);

// reducer
export function reducer(state = new State(), action:Object):State {
    const { type } = action;
    if (type in actionHandlers) {
        return actionHandlers[type](state, action);
    } else {
        return state;
    }
}

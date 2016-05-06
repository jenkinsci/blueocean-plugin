import { createSelector } from 'reselect';
import { actionHandlers } from './actions';
import { State } from '../components/records';
//we do not expose the root store
const adminStore = state => state.adminStore;
export const pipelines = createSelector([adminStore], store => store.pipelines);
export const pipeline = createSelector([adminStore], store => store.pipeline);
export const runs = createSelector([adminStore], store => store.runs);
export const currentRuns = createSelector([adminStore], store => store.currentRuns);
export const branches = createSelector([adminStore], store => store.branches);
export const currentBranches = createSelector([adminStore], store => store.currentBranches);
export const isMultiBranch = createSelector (
    [pipeline], (pipeline) => {
      if (pipeline) {
          return !!pipeline.branchNames;
      } else {
          return null;
      }
    }
);

// reducer
export function reducer(state = new State(), action:Object):State {
    const { type } = action;
    if (type in actionHandlers) {
        return actionHandlers[type](state, action);
    }
    return state;
}

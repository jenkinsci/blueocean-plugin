import { createSelector } from 'reselect';
import { actionHandlers } from './actions';
import { State } from '../components/records';
// we do not expose the root stores
const adminStore = state => state.adminStore;
const location = (state) => state.location;
export const previous = createSelector([location], store => store.previous);
export const current = createSelector([location], store => store.current);
export const messages = createSelector([adminStore], store => store.messages);
export const pipelines = createSelector([adminStore], store => store.pipelines);
export const pipeline = createSelector([adminStore], store => store.pipeline);
export const runs = createSelector([adminStore], store => store.runs);
export const currentRuns = createSelector([adminStore], store => store.currentRuns);
export const branches = createSelector([adminStore], store => store.branches);
export const logs = createSelector([adminStore], store => store.logs);
export const node = createSelector([adminStore], store => store.node);
export const nodes = createSelector([adminStore], store => store.nodes);
export const steps = createSelector([adminStore], store => store.steps);
export const currentBranches = createSelector([adminStore], store => store.currentBranches);
export const testResults = createSelector([adminStore], store => store.testResults);
export const isMultiBranch = createSelector(
    [pipeline], (pipe) => {
        if (pipe && pipe.organization) {
            return !!pipe.branchNames;
        }
        return null;
    }
);
export const capabilities = createSelector([adminStore], store => store.capabilities);

// reducer
export function reducer(state = new State(), action:Object):State {
    const { type } = action;
    if (type in actionHandlers) {
        return actionHandlers[type](state, action);
    }
    return state;
}

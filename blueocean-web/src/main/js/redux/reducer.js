import { actionHandlers } from './actions';
import { State } from './reduxState';

// FIXME: we need something that gets all reducer definitions from the extensions
// reducer
export function reducer(state = new State(), action:Object):State {
    const { type } = action;
    if (type in actionHandlers) {
        return actionHandlers[type](state, action);
    } else {
        return state;
    }
}

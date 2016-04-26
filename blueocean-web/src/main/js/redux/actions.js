import Immutable from 'immutable';
import keymirror from 'keymirror';

//import fetch from 'isomorphic-fetch';

// FIXME: we need something that gets all action definitions from the extensions
// Actions -> comes from a module
export const ACTION_TYPES = keymirror({
    CLEAR_PLUGIN_DATA: null,
    FETCH_PLUGIN_DATA: null,
    SET_PLUGIN_DATA: null,
});

export const actionHandlers = {
    [ACTION_TYPES.CLEAR_PLUGIN_DATA](state) {
        return state.set('plugins', Immutable.OrderedMap());
    },
    [ACTION_TYPES.FETCH_PLUGIN_DATA](state, {}): State {
        return state.set('isFetching', !state.isFetching);
    },
    [ACTION_TYPES.SET_PLUGIN_DATA](state, { payload }): State {
        return state.set('plugins', payload);
    },
};

export const actions = {
    clearPluginData: () => ({type: ACTION_TYPES.CLEAR_PLUGIN_DATA}),
    fetchPluginData: () => ({type: ACTION_TYPES.FETCH_PLUGIN_DATA}),
    generatePluginData(query = {}) {
        console.log('eee')
        dispatch({
            type: ACTION_TYPES.SET_PLUGIN_DATA,
            payload: {}
          });
    },
};

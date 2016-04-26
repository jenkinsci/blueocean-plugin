import Immutable from 'immutable';
import keymirror from 'keymirror';

//import fetch from 'isomorphic-fetch';

// FIXME: we need something that gets all action definitions from the extensions
// Actions -> comes from a module
export const ACTION_TYPES = keymirror({
    CLEAR_PIPELINE_DATA: null,
    SET_PIPELINE_DATA: null,
});

export const actionHandlers = {
    [ACTION_TYPES.CLEAR_PIPELINE_DATA](state) {
        return state.set('pipelines', Immutable.OrderedMap());
    },
    [ACTION_TYPES.SET_PIPELINE_DATA](state, { payload }): State {
        return state.set('pipelines', payload);
    },
};

export const actions = {
    clearPipelineData: () => ({type: ACTION_TYPES.CLEAR_PIPELINE_DATA}),
    generatePipelineData(config = {}) {
        return (dispatch) => {
            console.log('eee')
            dispatch({
                type: ACTION_TYPES.SET_PIPELINE_DATA,
                payload: {}
            });
        };
    },
};

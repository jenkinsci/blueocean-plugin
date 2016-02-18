/**
 * TODO: Docs
 */

import { createStore, combineReducers, applyMiddleware, bindActionCreators } from 'redux';

export const ADD_PIPELINE = "pipelineStoreReducer.addPipeline"; // TODO: Cook up something better than this hardcoded const bs

const initialDebugState = { // TODO: Replace this with something emptier once we're linked up to REST API
    pipelines: [
        {name:"Ken", status:"green"},
        {name:"Ryu", status:"green"},
        {name:"Sagat", status:"red"},
        {name:"Liu Kang", status:"green"}
    ]
};

export function pipelineStoreReducer(state = initialDebugState, action) {
    switch (action.type) {
        case ADD_PIPELINE:
            return Object.assign({}, state,
                {pipelines: [...state.pipelines, action.pipeline]}
            );
    }
    return state;
}
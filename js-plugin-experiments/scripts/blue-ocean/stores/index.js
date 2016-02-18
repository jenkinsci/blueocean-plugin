/**
 * TODO: Docs
 */

import { createStore, combineReducers, applyMiddleware, bindActionCreators } from 'redux';
import {pipelineStoreReducer, ADD_PIPELINE} from './pipeline-store';

const reducers = combineReducers({
    pipelines: pipelineStoreReducer
});

const middlewares = undefined; // For now.

export const store = createStore(reducers, {}, middlewares);

// Group actions from all the "sub stores" for now, until we have a more elegant solution
export const actions = {
    ADD_PIPELINE
};
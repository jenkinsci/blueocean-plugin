import { applyMiddleware, combineReducers, compose, createStore } from 'redux';
import thunk from 'redux-thunk';

export function createAppStore(rootReducer):Object {
    const finalCreateStore = compose(
      applyMiddleware(thunk)
    )(createStore);

    return finalCreateStore(rootReducer);
}



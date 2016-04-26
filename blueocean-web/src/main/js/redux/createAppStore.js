import { applyMiddleware, combineReducers, compose, createStore } from 'redux';
import thunk from 'redux-thunk';

import { reducer } from './reducer';

export function createAppStore():Object {
    const finalCreateStore = compose(
      applyMiddleware(thunk)
    )(createStore);

    const rootReducer = combineReducers({
        extensions: reducer
    });

    return finalCreateStore(rootReducer);
}



import { applyMiddleware, compose, createStore } from 'redux';
import thunk from 'redux-thunk';

export function configureStore(rootReducer): Object {
    const finalCreateStore = compose(applyMiddleware(thunk), window.devToolsExtension ? window.devToolsExtension() : f => f)(createStore);

    return finalCreateStore(rootReducer);
}

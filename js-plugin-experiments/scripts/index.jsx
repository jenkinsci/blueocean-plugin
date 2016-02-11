import React from 'react';
import {render} from 'react-dom';
import App from './App';

import { Router, Route, Link } from 'react-router';
import createBrowserHistory from 'history/lib/createBrowserHistory';

import {HomePage, AboutPage, NotFoundPage, AlienPage} from './pages.jsx';

import {PluginManager} from './blue-ocean';

// ------------------------------------------------------------------------------------
// THIS IS ALL BAD

function addDynamicRoute() {
    //if (routes.length == 3) {
    console.log("adding Dynamic route");
    //routes.unshift({ path: "/dynamic", name: "added dynamically",    component: ThirdPage,    key: (routeKey++)});
    //renderApp();
    //}
}

const pluginManager = new PluginManager();
const registerPlugin = pluginManager.registerPlugin.bind(pluginManager);

window.$HACK = {
    addDynamicRoute,
    registerPlugin
};

// ^^^ ALL BAD
// ------------------------------------------------------------------------------------

render(
    <Router history={createBrowserHistory()}>
        <Route component={App}>
            <Route path="/" component={HomePage}/>
            <Route path="/about" component={AboutPage}/>
            <Route path="/alien" component={AlienPage}/>
            <Route path="*" component={NotFoundPage}/>
        </Route>
    </Router>,
    document.getElementById('root')
);

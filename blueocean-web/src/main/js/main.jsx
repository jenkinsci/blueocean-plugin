import React, { Component, PropTypes } from 'react';
import { render } from 'react-dom';
import { Router, Route, Link, useRouterHistory, IndexRedirect } from 'react-router';
import { createHistory } from 'history';
import { i18nTranslator, AppConfig, Security, UrlConfig, Utils, sseService, locationService, NotFound } from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { Provider, configureStore, combineReducers} from './redux';
import rootReducer, { ACTION_TYPES } from './redux/router';
import Config from './config';
import { ToastDrawer } from './components/ToastDrawer';
import { DevelopmentFooter } from './DevelopmentFooter';
import { useStrict } from 'mobx';
useStrict(true);


let config; // Holder for various app-wide state

const translate = i18nTranslator('blueocean-web');

function loginOrLogout(t) {
    if (Security.isSecurityEnabled()) {
        if (Security.isAnonymousUser()) {
            const loginUrl = `${UrlConfig.getJenkinsRootURL()}/${AppConfig.getLoginUrl()}?from=${encodeURIComponent(Utils.windowOrGlobal().location.pathname)}`;
            return (<a href={loginUrl} className="btn-link">{t('login', {
                defaultValue: 'login',
            })}</a>);
        } else {
            const logoutUrl = `${UrlConfig.getJenkinsRootURL()}/logout`;
            return (<a href={logoutUrl} className="btn-link">{t('logout', {
                defaultValue: 'logout',
            })}</a>);
        }
    }
}

// Show link when only when someone is logged in...unless security is not configured,
// then show it anyway.
const AdminLink = (props) => {
    const { t } = props;
    const showLink = !Security.isSecurityEnabled() || !Security.isAnonymousUser();

    if (showLink) {
        var adminCaption = t('administration', {
            defaultValue: 'Administration',
        });
        return <a href={`${UrlConfig.getJenkinsRootURL()}/manage`}>{adminCaption}</a>;          
    }

    return null;
};

AdminLink.propTypes = {
    t: PropTypes.func,
};

/**
 * Root Blue Ocean UI component
 */
class App extends Component {

    getChildContext() {
        return {config};
    }

    render() {
        const { location } = this.context;

        var pipeCaption = translate('pipelines', {
            defaultValue: 'Pipelines',
        });
        return (
            <div className="Site">
                <header className="Site-header">
                    <div className="global-header">
                        <Extensions.Renderer extensionPoint="jenkins.logo.top"/>
                        <nav>
                            <Link query={location.query} to="/pipelines">{pipeCaption}</Link>
                            <AdminLink t={translate} />
                        </nav>
                        <div className="button-bar layout-small inverse">
                            { loginOrLogout(translate) }
                        </div>
                    </div>
                </header>
                <main className="Site-content">
                    {this.props.children /* Set by react-router */ }
                </main>
                <footer className="Site-footer">
                    <DevelopmentFooter />
                </footer>
                <ToastDrawer />
            </div>
        );
    }
}

App.propTypes = {
    children: PropTypes.node,
};

App.childContextTypes = {
    config: PropTypes.object
};

App.contextTypes = {
    location: PropTypes.object.isRequired,
};

const closeHandler = (props) => props.onClose || {};
function makeRoutes(routes) {
    // Build up our list of top-level routes RR will ignore any non-route stuff put into this list.
    const appRoutes = [
        ...routes,
        // FIXME: Not sure best how to set this up without the hardcoded IndexRedirect :-/
        <IndexRedirect to="/pipelines" />,
        <Route path="*" component={NotFound}/>
    ];

    const routeProps = {
        path: "/",
        component: App,
    };

    return React.createElement(Route, routeProps, ...appRoutes);
}


function startApp(routes, stores) {

    const rootElement = document.getElementById("root");
    const headElement = document.getElementsByTagName("head")[0];

    // Look up where the Blue Ocean app is hosted
    let appURLBase = headElement.getAttribute("data-appurl");

    if (typeof appURLBase !== "string") {
        appURLBase = '';
    }

    // Look up some other URLs we may need
    const rootURL = headElement.getAttribute("data-rooturl");
    const resourceURL = headElement.getAttribute("data-resurl");
    const adjunctURL = headElement.getAttribute("data-adjuncturl");

    // Stash urls in our module-local var, so that App can put them on context.
    config = new Config({
        appURLBase,
        rootURL,
        resourceURL,
        adjunctURL
    });

    // Using this non-default history because it allows us to specify the base url for the app
    const history = useRouterHistory(createHistory)({
        basename: appURLBase
    });

    // get all ExtensionPoints related to redux-stores
    let store;
    if (stores.length === 0) {
        // if we do not have any stores we only add the location store
        store = configureStore(combineReducers(rootReducer));
    } else {
        // some plugins provide they own store so combining with loction store
        store = configureStore(combineReducers(
          Object.assign({}, ...stores, rootReducer))
        );
    }

    // on each change of the url we need to update the location object
    history.listen(newLocation => {
        const { dispatch, getState } = store;
        const { current } = getState().location;

        // don't store current as previous if doing a REPLACE (or on the first request)
        if (newLocation.action !== 'REPLACE' && current) {
            dispatch({
                type: ACTION_TYPES.SET_LOCATION_PREVIOUS,
                payload: current,
            });
        }

        dispatch({
            type: ACTION_TYPES.SET_LOCATION_CURRENT,
            payload: newLocation.pathname,
        });

        locationService.setCurrent(newLocation);
    });

    sseService._initListeners();

    // Start React
    render(
        <Provider store={store}>
            <Router history={history}>{ makeRoutes(routes) }</Router>
        </Provider>
      , rootElement);
}

Extensions.store.getExtensions(['jenkins.main.routes', 'jenkins.main.stores'], (routes = [], stores = []) => {
    startApp(routes, stores);
});

// Enable page reload.
require('./reload');

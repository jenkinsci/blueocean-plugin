import React, { Component, PropTypes } from 'react';
import { render } from 'react-dom';
import { Router, Route, Link, useRouterHistory, IndexRedirect } from 'react-router';
import { createHistory } from 'history';
import {
    logging, i18nTranslator, AppConfig, Security, UrlConfig, Utils, sseService, locationService, NotFound, SiteHeader, toClassicJobPage, User, loadingIndicator, LoginButton,
} from '@jenkins-cd/blueocean-core-js';
import Extensions from '@jenkins-cd/js-extensions';

import { Provider, configureStore, combineReducers} from './redux';
import rootReducer, { ACTION_TYPES } from './redux/router';
import Config from './config';
import { ToastDrawer } from './components/ToastDrawer';
import { BackendConnectFailure } from './components/BackendConnectFailure';
import { DevelopmentFooter } from './DevelopmentFooter';
import { useStrict } from 'mobx';
import { Icon } from '@jenkins-cd/design-language';
import ErrorUtils from './ErrorUtils';

useStrict(true);

const LOGGER = logging.logger('io.jenkins.blueocean.web.routing');

let config; // Holder for various app-wide state

const translate = i18nTranslator('blueocean-web');

// Show link when only when someone is logged in...unless security is not configured,
// then show it anyway.
const AdminLink = (props) => {
    const { t } = props;

    const user = User.current();
    const showLink = !Security.isSecurityEnabled() || user && user.isAdministrator;

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

        const pipeCaption = translate('pipelines', {
            defaultValue: 'Pipelines',
        });

        const topNavLinks = [
            <Extensions.Renderer extensionPoint="jenkins.blueocean.top.pipelines" />,
            <Extensions.Renderer extensionPoint="jenkins.blueocean.top.links" />,
            <Extensions.Renderer extensionPoint="jenkins.blueocean.top.admin">
                <AdminLink t={translate} />
            </Extensions.Renderer>,
        ];

        let classicUrl = toClassicJobPage(window.location.pathname);
        if (classicUrl) {
            // prepend with the jenkins root url
            classicUrl = UrlConfig.getJenkinsRootURL() + classicUrl;
        } else {
            classicUrl = UrlConfig.getJenkinsRootURL();
        }

        // Make sure there's a leading slash so that
        // the url is rooted...
        if (!classicUrl || classicUrl === '') {
            classicUrl = '/';
        } else if (classicUrl.charAt(0) !== '/') {
            classicUrl = '/' + classicUrl;
        }

        const userComponents = [
            <Extensions.Renderer extensionPoint="jenkins.blueocean.top.go.classic">
                <div className="user-component icon" title={translate('go.to.classic', { defaultValue: 'Go to classic' })}>
                    <a className="main_exit_to_app" href={classicUrl}><Icon icon="ActionExitToApp" /></a>
                </div>
            </Extensions.Renderer>,
            <Extensions.Renderer extensionPoint="jenkins.blueocean.top.login">
                <LoginButton className="user-component button-bar layout-small inverse" translate={translate} />
            </Extensions.Renderer>
        ];

        const homeURL = config.getAppURLBase();

        return (
            <div className="Site">
                <SiteHeader homeURL={homeURL} topNavLinks={topNavLinks} userComponents={userComponents}/>

                <main className="Site-content">
                    {this.props.children /* Set by react-router */ }
                </main>
                <footer className="Site-footer">
                    {/* FIXME: jenkins.logo.top is being used to force CSS loading */}
                    <Extensions.Renderer extensionPoint="jenkins.logo.top"/>
                    <DevelopmentFooter />
                </footer>
                <ToastDrawer />
                <BackendConnectFailure />
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

    if (LOGGER.isDebugEnabled()) {
        debugRoutes(appRoutes);
    }

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
    const serverBrowserTimeSkewMillis = headElement.getAttribute("data-servertime") - Date.now();
    // Stash urls in our module-local qwqvar, so that App can put them on context.
    config = new Config({
        appURLBase,
        rootURL,
        resourceURL,
        adjunctURL,
        serverBrowserTimeSkewMillis,
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
        // some plugins provide they own store so combining with location store
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
    loadingIndicator.setDarkBackground();
    startApp(routes, stores);
});

// Enable page reload.
require('./reload');

function debugRoutes(appRoutes) {
    try {
        const NODE_END_MARKER = 'NODE_END_MARKER';
        const MAX_ITERATIONS = 500;
        const routes = appRoutes.slice();
        // tracks the fully-qualified path as we walk the route tree
        const pathParts = [];
        let iterations = 0;

        while (routes.length) {
            const currentRoute = routes.shift();

            // skip over Redirect, IndexRedirect, etc
            if (currentRoute && currentRoute.type && currentRoute.type.displayName === 'Route') {
                const fullPath = [].concat(pathParts, currentRoute.props.path);
                // this is the fully-qualified route path
                LOGGER.debug(`route: ${fullPath.join('/')}`);

                if (currentRoute.props.children) {
                    // when descending into a node we want to augment the fully-qualified path
                    const path = currentRoute.props.path !== '/' ? currentRoute.props.path : '';
                    pathParts.push(path);
                    // add a 'node end' marker at the end so we can shorten the fully-qualified path later
                    routes.unshift(...currentRoute.props.children, NODE_END_MARKER);
                }
            } else if (currentRoute === NODE_END_MARKER) {
                pathParts.pop();
            }

            iterations++;

            if (iterations >= MAX_ITERATIONS) {
                LOGGER.warn(`exceeded max iteration count of ${MAX_ITERATIONS}. aborting route dump!`);
                break;
            }
        }
    } catch (error) {
        LOGGER.warn(`error parsing route data: ${error}. aborting route dump!`);
    }
}

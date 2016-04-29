import React, { Component, PropTypes } from 'react';
import { render } from 'react-dom';
import { Router, Route, IndexRoute, browserHistory, Link, useRouterHistory, IndexRedirect } from 'react-router';
import { createHistory, useBasename } from 'history';
import { DevelopmentFooter } from './DevelopmentFooter';

import { ExtensionPoint } from '@jenkins-cd/js-extensions';

import Config from './config';


var config; // Holder for various app-wide state

/**
 * Root Blue Ocean UI component
 */
class App extends Component {

    getChildContext() {
        return {config};
    }

    render() {
        return (
            <div className="Site">
                <div id="outer">
                    <header className="global-header">
                        <ExtensionPoint name="jenkins.logo.top"/>
                        <nav>
                            <Link to="/pipelines">Pipelines</Link>
                            <a href="#">Applications</a>
                            <a href="#">Reports</a>
                            <a href="#">Administration</a>
                        </nav>
                    </header>
                    <main>
                        {this.props.children /* Set by react-router */ }
                    </main>
                </div>
                <DevelopmentFooter />
            </div>
        );
    }
}

App.propTypes = {
    children: PropTypes.node
};

App.childContextTypes = {
    config: PropTypes.object
};

class NotFound extends Component {
    // FIXME: We're going to need to polish this up at some point
    render() {
        return <h1>Not found</h1>;
    }
}

function makeRoutes() {
    // Build up our list of top-level routes RR will ignore any non-route stuff put into this list.
    const appRoutes = [
        ...ExtensionPoint.getExtensions("jenkins.main.routes"),
        // FIXME: Not sure best how to set this up without the hardcoded IndexRedirect :-/
        <IndexRedirect to="/pipelines" />,
        <Route path="*" component={NotFound}/>
    ];

    const routeProps = {
        path: "/",
        component: App
    };

    return React.createElement(Route, routeProps, ...appRoutes);
}


function startApp() {

    const rootElement = document.getElementById("root");
    const headElement = document.getElementsByTagName("head")[0];

    // Look up where the Blue Ocean app is hosted
    var appURLBase = headElement.getAttribute("data-appurl");

    if (typeof appURLBase !== "string") {
        appURLBase = "/";
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

    // Start React
    render(<Router history={history}>{ makeRoutes() }</Router>, rootElement);
}

ExtensionPoint.registerExtensionPoint("jenkins.main.routes", (extensions) => {
    startApp();
});

import React, {Component} from 'react';
import {render} from 'react-dom';

import {extensionPointStore, ExtensionPoint} from './blue-ocean';

import { Router, Route, Link } from 'react-router';
import createBrowserHistory from 'history/lib/createBrowserHistory';

import {AboutPage, NotFoundPage, AlienPage} from './pages';
import {PipelinesPage} from './plugins/pipelines';

import {components} from 'jenkins-design-language';

let {
    WeatherIcon,
    Page,
    GlobalNav,
    GlobalHeader,
    PageHeader,
    Title,
    PageTabs
    } = components;

require('./blue-ocean/register-plugins.js'); // this will be done by the server somehow

/**
 * Root Blue Ocean UI component
 */
class App extends Component {
    render() {
        return (
            <Page>
                <GlobalHeader>
                    <GlobalNav>
                        <Link to="/">Home</Link>
                        <Link to="/about">About</Link>
                        <Link to="/Alien">Alien</Link>
                        <Link to="/dynamic">Dynamic</Link>
                        <ExtensionPoint name="jenkins.topNavigation.menu"/>
                    </GlobalNav>
                </GlobalHeader>
                <PageHeader>
                    <Title>FooBar</Title>
                    <PageTabs>
                        <a href="#">Alpha</a>
                        <a href="#">Bravo</a>
                        <a href="#">Charlie</a>
                    </PageTabs>
                </PageHeader>
                <main>
                    {/* children currently set by router */}
                    {this.props.children}
                </main>
                <footer>
                    <p>This is a footer. I'm sure you'll agree.</p>
                </footer>
            </Page>
        );
    }
}


/**
 * We need some work to make routes both dynamic, and work with extensions that run in a sub tree.
 * so as it is now, the routes are just hard coded to the components
 * TODO:  ideally there would be one ExtensionPoint and they can contribute to the routes.
 */
render(
    <Router history={createBrowserHistory()}>
        <Route component={App}>
            <Route path="/" component={PipelinesPage}/>
            <Route path="/about" component={AboutPage}/>
            <Route path="/alien" component={AlienPage}/>
            <Route path="*" component={NotFoundPage}/>
        </Route>
    </Router>,
    document.getElementById('root')
);

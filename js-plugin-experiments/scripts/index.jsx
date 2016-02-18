import React, {Component} from 'react';
import {render} from 'react-dom';

import {extensionPointStore, ExtensionPoint} from './blue-ocean';


import { Router, Route, Link } from 'react-router';
import createBrowserHistory from 'history/lib/createBrowserHistory';

import {HomePage, AboutPage, NotFoundPage, AlienPage} from './pages.jsx';


require('./blue-ocean/register-plugins.js'); // this will be done by the server somehow


/** 
 * Root Blue Ocean UI component 
 */
class App extends Component {
    render() {
        return (
            <div id="outer">
                <header>
                    <img src="/resources/logo.png" width="150" id="jenkins-logo"/>
                    <nav>
                        <Link to="/">Home</Link>
                        <Link to="/about">About</Link>
                        <Link to="/Alien">Alien</Link>
                        <Link to="/dynamic">Dynamic</Link>
                        <ExtensionPoint name="jenkins.topNavigation.menu" />
                    </nav>                    
                </header>
                <main>
                    {/* children currently set by router */}
                    {this.props.children}
                </main>
                <footer>
                    <p>This is a footer. I'm sure you'll agree.</p>
                </footer>
            </div>
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
            <Route path="/" component={HomePage}/>
            <Route path="/about" component={AboutPage}/>
            <Route path="/alien" component={AlienPage}/>
            <Route path="*" component={NotFoundPage}/>
        </Route>
    </Router>,
    document.getElementById('root')
);

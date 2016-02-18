import React from 'react';
import {render} from 'react-dom';
import App from './App';

import { Router, Route, Link } from 'react-router';
import createBrowserHistory from 'history/lib/createBrowserHistory';

import {HomePage, AboutPage, NotFoundPage, AlienPage} from './pages.jsx';


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

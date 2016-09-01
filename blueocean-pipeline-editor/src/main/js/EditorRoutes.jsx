// @flow

import { Route, Redirect, IndexRoute, IndexRedirect } from 'react-router';
import React from 'react';
import { EditorDemo } from './components/EditorDemo';

export default (
    <Route path="/pipeline-editor-demo" component={EditorDemo}/>
);

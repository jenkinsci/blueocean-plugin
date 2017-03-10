// @flow

import React from 'react';
import { Route } from 'react-router';
import { EditorPage } from './EditorPage';
import { EditorPage as FullScreenEditor } from './components/editor/EditorPage';

export default 
    <Route>
        <Route path="/pipeline-editor" component={FullScreenEditor} />
        <Route path="/organizations/:organization/pipeline-editor/:pipeline/(:branch)(/)" component={EditorPage} />
    </Route>
;

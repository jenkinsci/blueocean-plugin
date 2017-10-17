// @flow

import React from 'react';
import { Route } from 'react-router';
import { EditorPage } from './EditorPage';

export default 
    <Route>
        <Route path="/organizations/:organization/pipeline-editor/(:pipeline/)(:branch/)" component={EditorPage} />
    </Route>
;

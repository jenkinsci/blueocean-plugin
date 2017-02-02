// @flow

import { Route, Redirect, IndexRoute, IndexRedirect } from 'react-router';
import React from 'react';
import { EditorPreview } from './components/EditorPreview';

export default (
    <Route path="/pipelines/pipeline-editor-preview" component={EditorPreview}/>
);

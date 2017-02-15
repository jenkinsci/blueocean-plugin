// @flow

import React, { Component, PropTypes } from 'react';
import { EditorPage } from './editor/EditorPage';
import { EditorMain } from './editor/EditorMain';
import Extensions from '@jenkins-cd/js-extensions';

/**
 This is basically adapted from the Storybooks entry, for the purposes of connecting a demo into the main appendEvent
 */
export class EditorPreview extends Component {
    constructor() {
        super();
    }
    render() {
        return (
            <EditorPage>
                <Extensions.Renderer extensionPoint="pipeline.editor.css"/>
                <EditorMain />
            </EditorPage>
        );
    }
}

export default EditorPreview;

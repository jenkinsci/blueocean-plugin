// @flow

import React, { Component, PropTypes } from 'react';
import { EditorPage } from './editor/EditorPage';
import { EditorMain } from './editor/EditorMain';
import Extensions from '@jenkins-cd/js-extensions';

const pageStyles = {
    display: "flex",
    width: "100%",
    height: "100%"
};

/**
 This is basically adapted from the Storybooks entry, for the purposes of connecting a demo into the main appendEvent
 */
export class EditorDemo extends Component {
    constructor() {
        super();
    }
    render() {
        return (
            <EditorPage style={pageStyles}>
                <Extensions.Renderer extensionPoint="pipeline.editor.css"/>
                <EditorMain />
            </EditorPage>
        );
    }
}

export default EditorDemo;

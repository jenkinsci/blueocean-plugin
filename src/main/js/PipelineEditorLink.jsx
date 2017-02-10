// @flow

import React, { Component } from 'react';
import { Link } from 'react-router';
import Extensions from '@jenkins-cd/js-extensions';

function PipelineEditorLink() {
    return (
        <Link className="pipeline-editor-link" to="/pipelines/pipeline-editor-preview">
            <Extensions.Renderer extensionPoint="pipeline.editor.css"/>
            Pipeline Editor
        </Link>
    );
}

export default PipelineEditorLink;

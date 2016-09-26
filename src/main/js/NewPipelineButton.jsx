// @flow

import React, { Component } from 'react';
import { Link } from 'react-router';

function NewPipelineButton() {
    return (
        <Link className="btn-secondary inverse" to="/pipeline-editor-demo">
            New Pipeline
        </Link>
    );
}

export default NewPipelineButton;

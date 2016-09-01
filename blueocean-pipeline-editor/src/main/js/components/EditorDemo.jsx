// @flow

import React, { Component, PropTypes } from 'react';
import { EditorPage } from './editor/EditorPage';
import { EditorMain } from './editor/EditorMain';

import type {StepInfo, StageInfo} from './editor/common';

const pageStyles = {
    display: "flex",
    width: "100%",
    height: "100%"
};

/// Simple helpers for data generation

var __id = 1;

function makeStage(name, children = []):StageInfo {
    const id = __id++;
    return {name, children, id};
}

function makeStep(type:string, label:string, nestedSteps?:Array<StepInfo>):StepInfo {
    const id = __id++;
    const children = nestedSteps || [];
    const isContainer = !!children.length;
    const data = null; // TODO: Put stuff here at some point
    return {
        id,
        type,
        label,
        isContainer,
        children,
        data
    };
}

/**
 This is basically adapted from the Storybooks entry, for the purposes of connecting a demo into the main appendEvent
 */
export class EditorDemo extends Component {
    render() {

        let testStages = [
            makeStage("Internet Explorer"),
            makeStage("Chrome")
        ];

        let stages = [
            makeStage("Build"),
            makeStage("Test"),
            makeStage("Browser Tests", testStages),
            makeStage("Deploy")
        ];

        let stageSteps = {};

        stageSteps[testStages[0].id] = [makeStep("script", "Run Script")];
        stageSteps[testStages[1].id] = [makeStep("script", "Run Script")];

        stageSteps[stages[0].id] = [makeStep("script", "Run Script")];
        stageSteps[stages[1].id] = [makeStep("script", "Run Script")];
        stageSteps[stages[2].id] = [makeStep("script", "Run Script")];

        return (
            <EditorPage style={pageStyles}>
                <EditorMain stages={stages} stageSteps={stageSteps}/>
            </EditorPage>
        );
    }
}

export default EditorDemo;

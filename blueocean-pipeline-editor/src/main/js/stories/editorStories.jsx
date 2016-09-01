// @flow

import React, {Component, PropTypes} from 'react';
import { storiesOf } from '@kadira/storybook';
import {EditorPipelineGraph, defaultLayout} from '../components/editor/EditorPipelineGraph';
import {EditorStepList} from '../components/editor/EditorStepList';
import {EditorMain} from '../components/editor/EditorMain';
import {EditorStepDetails} from '../components/editor/EditorStepDetails';
import {EditorPage} from '../components/editor/EditorPage';

import type {StepInfo, StageInfo} from '../components/editor/common';

//--[ Globals and consts ]----------------------------------------------------------------------------------------------

// Leave these here for now, we'll need to re-think this once there's a bunch of different types / plugins etc
// TODO: A type for these, for now?
const st_echo = "echo";
const st_shell = "script";
const st_retry = "retry";

let __id = 1;

//--[ Story Index ]-----------------------------------------------------------------------------------------------------

storiesOf('Pipeline Editor Main', module)
    .add('Basic', renderMainBasic)
    .add('Wrapped in page', renderMainInPage)
;

storiesOf('Pipeline Editor Graph', module)
    .add('Basic', renderPiplineFlat)
    .add('Mixed', renderPipelineMixed)
    .add('Duplicate Names', renderPipelineDupNames)
;

storiesOf('Pipeline Editor Step List', module)
    .add('Basic', renderStepsBasic)
    .add('selectedStep', renderStepsSelected)
    .add('Nesting', renderStepsNesting)
    .add('More Nesting', renderStepsNestingDeeper)
;

storiesOf('Pipeline Editor Step Details', module)
    .add('Basic', renderStepDetailsBasic)
;

//--[ Main Wrapper ]----------------------------------------------------------------------------------------------------

const mainContainerStyle = {
    margin: "1em",
    boxShadow: "0px 9px 16px 0px rgba(0,0,0,0.24)"
};

function renderMainBasic() {

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

    stageSteps[testStages[0].id] = [makeStep(st_shell, "Run Script")];
    stageSteps[testStages[1].id] = [makeStep(st_shell, "Run Script")];

    stageSteps[stages[0].id] = [makeStep(st_echo, "Echo"), makeStep(st_shell, "Run Script"), makeStep(st_echo, "Echo")];

    stageSteps[stages[1].id] = [makeStep(st_shell, "Run Script")];

    stageSteps[stages[2].id] = [
        makeStep(st_echo, "Echo"),
        makeStep(st_shell, "Run Script"),
        makeStep(st_retry, "Retry", [
            makeStep(st_echo, "Echo"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_echo, "Echo")])];

    return (
        <div style={mainContainerStyle}>
            <EditorMain stages={stages} stageSteps={stageSteps}/>
        </div>
    );
}

function renderMainInPage() {

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

    stageSteps[testStages[0].id] = [makeStep(st_shell, "Run Script")];
    stageSteps[testStages[1].id] = [makeStep(st_shell, "Run Script")];

    stageSteps[stages[0].id] = [makeStep(st_echo, "Echo"), makeStep(st_shell, "Run Script"), makeStep(st_echo, "Echo")];

    stageSteps[stages[1].id] = [makeStep(st_shell, "Run Script")];

    stageSteps[stages[2].id] = [
        makeStep(st_echo, "Echo"),
        makeStep(st_shell, "Run Script"),
        makeStep(st_retry, "Retry", [
            makeStep(st_echo, "Echo"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_echo, "Echo")])];

    return (
        <div style={mainContainerStyle}>
            <EditorPage>
                <EditorMain stages={stages} stageSteps={stageSteps}/>
            </EditorPage>
        </div>
    );
}

//--[ Step Details Editor ]---------------------------------------------------------------------------------------------

const stepDetailssContainerStyle = {
    border: "solid 1px #ccc",
    maxWidth: "40em",
    margin: "2em",
    display: "flex",
    flex: "1",
    flexDirection: "column",
    minHeight: "10em"
};

function renderStepDetailsBasic() {
    let step = makeStep(st_shell, "Run Script");
    return (
        <div style={stepDetailssContainerStyle}>
            <EditorStepDetails step={step}/>
        </div>
    );
}

//--[ Steps List ]------------------------------------------------------------------------------------------------------

const stepsContainerStyle = {maxWidth: "40em", margin: "2em"};

function renderStepsBasic() {
    const steps = [
        makeStep(st_echo, "Echo"),
        makeStep(st_shell, "Run Script"),
        makeStep(st_shell, "Run Script"),
        makeStep(st_shell, "Run Script")
    ];

    function addStepClicked() {
        console.log("Add step");
    }

    function stepSelected(step) {
        console.log("Step Selected", step);
    }

    function deleteStepClicked(step) {
        console.log("Delete step", step);
    }

    return (
        <div style={stepsContainerStyle}>
            <EditorStepList steps={steps}
                            onAddStepClick={addStepClicked}
                            onStepSelected={stepSelected}
                            onDeleteStepClick={deleteStepClicked}/>
        </div>
    );
}

function renderStepsSelected() {
    const steps = [
        makeStep(st_echo, "Echo"),
        makeStep(st_shell, "Run Script"),
        makeStep(st_shell, "Run Script"),
        makeStep(st_shell, "Run Script")
    ];

    return (
        <div style={stepsContainerStyle}>
            <EditorStepList steps={steps} selectedStep={steps[1]}/>
        </div>
    );
}

function renderStepsNesting() {
    const steps = [
        makeStep(st_echo, "Echo"),
        makeStep(st_shell, "Run Script"),
        makeStep(st_retry, "Retry", [
            makeStep(st_echo, "Echo"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_echo, "Echo")
        ]),
        makeStep(st_shell, "Run Script")
    ];

    return (
        <div style={stepsContainerStyle}>
            <EditorStepList steps={steps}/>
        </div>
    );
}

function renderStepsNestingDeeper() {
    const steps = [
        makeStep(st_echo, "Echo"),
        makeStep(st_retry, "Retry", [
            makeStep(st_echo, "Echo"),
            makeStep(st_shell, "Run Script"),
            makeStep(st_retry, "Retry", [
                makeStep(st_echo, "Echo"),
                makeStep(st_shell, "Run Script"),
                makeStep(st_shell, "Run Script"),
                makeStep(st_shell, "Run Script"),
                makeStep(st_echo, "Echo")
            ]),
            makeStep(st_echo, "Echo")
        ])
    ];

    return (
        <div style={stepsContainerStyle}>
            <EditorStepList steps={steps}/>
        </div>
    );
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

//--[ Pipeline Graph ]--------------------------------------------------------------------------------------------------

function renderPiplineFlat() {

    const stages = [
        makeStage("Ken"),
        makeStage("Sagat"),
        makeStage("Ryu"),
        makeStage("Guile")
    ];

    // Reduce spacing just to make this graph smaller
    const layout = {nodeSpacingH: 90};

    function onNodeClick(a, b) {
        console.log("Node clicked", a, b);
    }

    return (
        <div>
            <EditorPipelineGraph stages={stages} layout={layout} onNodeClick={onNodeClick}/>
        </div>
    );
}

function renderPipelineDupNames() {

    const stages = [
        makeStage("Build"),
        makeStage("Test"),
        makeStage("Browser Tests", [
            makeStage("Internet Explorer"),
            makeStage("Chrome")
        ]),
        makeStage("Test"),
        makeStage("Staging"),
        makeStage("Production")
    ];

    return (
        <div>
            <EditorPipelineGraph stages={stages}/>
        </div>
    );
}

function renderPipelineMixed() {

    const stages = [
        makeStage("Build"),
        makeStage("Test", [
            makeStage("JUnit"),
            makeStage("DBUnit"),
            makeStage("Jasmine")
        ]),
        makeStage("Browser Tests", [
            makeStage("Firefox"),
            makeStage("Edge"),
            makeStage("Safari"),
            makeStage("Chrome")
        ]),
        makeStage("Dev"),
        makeStage("Staging"),
        makeStage("Production", [
            makeStage("us-east-1"),
            makeStage("us-west-1 "),
            makeStage("us-west-2"),
            makeStage("ap-south-1")
        ])
    ];

    return (
        <div>
            <EditorPipelineGraph stages={stages} selectedStage={stages[0]}/>
        </div>
    );
}

/// Simple helper for data generation
function makeStage(name, children = []):StageInfo {
    const id = __id++;
    return {name, children, id};
}

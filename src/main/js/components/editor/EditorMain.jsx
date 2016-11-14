// @flow

import React, { Component, PropTypes } from 'react';
import { EditorPipelineGraph } from './EditorPipelineGraph';
import { EditorStepList } from './EditorStepList';
import { EditorStepDetails } from './EditorStepDetails';
import { EmptyStateView } from '@jenkins-cd/design-language';
import { AddStepSelectionDialog } from './AddStepSelectionDialog';

import type { StageInfo, StepInfo } from './common';

type Props = {
    stages: Array<StageInfo>,
    stageSteps: {[stageId:number]: Array<StepInfo> }
};

type StageStepsMap = {[stageId:number]: Array<StepInfo> };

type State = {
    s?: bool, // TODO: RM
    stages: Array<StageInfo>,
    stageSteps: StageStepsMap,
    selectedStage: ?StageInfo,
    selectedStep: ?StepInfo
};


function _copy(obj) {
    // TODO: This is awful, use a lib
    return JSON.parse(JSON.stringify(obj));
}

let idSeq = -11111;

function createBasicStage(name:string):StageInfo {
    return {
        name,
        id: idSeq--,
        children: []
    };
}

const ss = {
    // TODO: RM
    position: "absolute",
    width: "500px",
    height: "500px",
    background: "#eee",
    backgroundImage: "url(http://www.iywib.com/soon_19.jpg)",
    marginLeft: "-250px",
    marginTop: "-250px",
    left: "50%",
    top: "50%"
};

/**
 * Search through candidates (and their children, recursively) to see if any is the parent of the stage with id childId
 */
function findParentStage(candidates:Array<StageInfo>, childId:number, safetyValve:number = 5):?StageInfo {
    // TODO: Move this to common.jsx
    // TODO: TESTS
    if (!candidates || candidates.length == 0 || !(safetyValve > 0)) {
        return null;
    }

    for (let maybeParent of candidates) {

        if (maybeParent.id === childId) {
            return null; // Should only happen on first iteration if childId has no parent
        }

        for (let child of maybeParent.children) {
            if (child.id === childId) {
                return maybeParent;
            }
        }

        let foundDescendent = findParentStage(maybeParent.children, childId, safetyValve - 1);

        if (foundDescendent) {
            return foundDescendent;
        }
    }

    return null;
}

type DefaultProps = typeof EditorMain.defaultProps;
export class EditorMain extends Component<DefaultProps, Props, State> {

    static defaultProps = {};

    //static propTypes = {...}
    // TODO: React proptypes ^^^

    props:Props;
    state:State;

    componentWillMount() {
        this.handleProps(
            this.props,
            {stages: [], stageSteps: {}},
            this.state);
    }

    componentWillReceiveProps(nextProps:Props) {
        this.handleProps(nextProps, this.props, this.state);
    }

    handleProps(nextProps:Props, oldProps:Props, state:State) {

        let updates = {},
            stagesChanged = false,
            stageStepsChanged = false;

        // Update stages list?
        if (nextProps.stages !== oldProps.stages) {
            let stages = _copy(nextProps.stages);
            updates.stages = stages;
            updates.selectedStage = (stages && stages[0]) || null;
            stagesChanged = true;
        }

        // Update stageSteps map?
        if (nextProps.stageSteps !== oldProps.stageSteps) {
            updates.stageSteps = _copy(nextProps.stageSteps);
            stageStepsChanged = true;
        }

        // If we've changed either stages or steps, we need a new selectedStep
        if (stagesChanged || stageStepsChanged) {
            let selectedStep = null; // If we don't find a first step we'll clear any old value

            let selectedStage = stagesChanged ? updates.selectedStage : state.selectedStage;
            let stageSteps = stageStepsChanged ? updates.stageSteps : state.stageSteps;

            if (selectedStage && stageSteps) {
                let stepsForStage = stageSteps[selectedStage.id];
                if (stepsForStage && stepsForStage.length) {
                    selectedStep = stepsForStage[0];
                }
            }

            updates.selectedStep = selectedStep;
        }

        this.setState(updates);
    }

    // TODO: For debugging, delete this as things firm up
    dumpState() {
        console.log("--[ DUMP STATE ]--------------------------------------------------------------------------------");

        console.log("Stages");

        const {stages, stageSteps, selectedStage, selectedStep} = this.state;

        let seenStageIds = [];
        let seenStages = [];

        function checkStage(stage) {

            let id = stage.id;

            if (seenStageIds.indexOf(id) >= 0) {
                console.error("DUPLICATE STAGE ID", id);
            }
            seenStageIds.push(id);
            seenStages.push(stage);
        }

        for (let parent of stages) {
            let parentId = parent.id;
            console.log(("       " + parentId).slice(-6), parent.name);

            checkStage(parent);

            if (parent.children.length === 1) {
                console.error("SPOILED BRAT ALERT! NO ONLY-CHILD ALLOWED");
            }

            for (let child of parent.children) {
                let childId = child.id;

                console.log("     ", ("       " + childId).slice(-6), child.name);

                checkStage(child);

                if (child.children.length) {
                    console.error("NO GRANDKIDS ALLOWED, GET OFF MY LAWN");
                }

            }
        }

        console.log("Stage steps");

        let stagesWithSteps = [];

        for (let stageId of Object.keys(stageSteps)) {
            stagesWithSteps.push(stageId);
            let stepsForStage = stageSteps[parseInt(stageId)];
            let message = "huh?";

            if (Array.isArray(stepsForStage)) {
                message = stepsForStage.length + " steps";
            }

            console.log(("      stage " + stageId).slice(-12), "=>", message);

            if (seenStageIds.indexOf(parseInt(stageId)) == -1) {
                console.error("THERE IS NO STAGE WITH ID", stageId);
            }
        }

        console.log("   seenStageIds", seenStageIds.sort().join(", "));
        console.log("stagesWithSteps", stagesWithSteps.sort().join(", "));

        if (selectedStage) {
            console.log("selectedStage", selectedStage.id, selectedStage.name);
            if (seenStages.indexOf(selectedStage) == -1) {
                console.error("SELECTED STAGE NOT FOUND IN GRAPH");
            }
            if (selectedStage.children.length) {
                console.error("SELECTED STAGE HAS CHILDREN ONLY CHILDLESS STAGES SELECTABLE");
            }
        }
        else {
            console.log("selectedStage is not set");
        }


        console.log("--[ END DUMP STATE ]----------------------------------------------------------------------------");
    }

    getStepsForStage(stage:StageInfo):Array<StepInfo> {
        const stageId = stage.id;
        return this.state.stageSteps[stageId] || [];
    }

    graphSelectedStageChanged(newSelectedStage:?StageInfo) {

        const {stageSteps} = this.state;
        let newSelectedStep = null;

        if (newSelectedStage) {
            const stepsForStage = stageSteps[newSelectedStage.id] || [];
            newSelectedStep = stepsForStage[0] || null;
        }

        this.setState({
            selectedStage: newSelectedStage,
            selectedStep: newSelectedStep
        });
    }

    createSequentialStage(name:string) {
        const {stages, stageSteps} = this.state;

        let newStage = createBasicStage(name);
        const stageId = newStage.id;

        this.setState({
            stages: [...stages, newStage],
            stageSteps: {...stageSteps, [stageId]: []},
            selectedStage: newStage,
            selectedStep: null
        });
    }

    createParallelStage(name:string, parentStage:StageInfo) {

        const oldStages = this.state.stages;
        const oldStageSteps = this.state.stageSteps;
        const parentStageIndex = oldStages.indexOf(parentStage);

        if (parentStageIndex === -1) {
            console.error("Could not find parent stage", parentStage, "in top-level stage list");
            return;
        }

        let updatedChildren = [...parentStage.children]; // Start with a shallow copy, we'll add one or two to this
        let updatedStageSteps = {...oldStageSteps}; // Shallow copy, we'll need to at least add one

        let newStage = createBasicStage(name);
        updatedStageSteps[newStage.id] = []; // Empty steps list for now, let the user work from a blank list

        if (parentStage.children.length == 0) {
            // Converting a normal stage with steps into a container of parallel branches, so there's more to do
            let zerothStage = createBasicStage(parentStage.name);

            // Move any steps from the parent stage into the new zeroth stage
            updatedStageSteps[zerothStage.id] = oldStageSteps[parentStage.id];
            updatedStageSteps[parentStage.id] = []; // Stages with children can't have steps

            updatedChildren.push(zerothStage);
        }

        updatedChildren.push(newStage); // Add the user's newStage to the parent's child list

        let updatedParentStage = {...parentStage, children: updatedChildren};

        // Build a new stages list
        let updatedStages = [
            ...(oldStages.slice(0, parentStageIndex)),
            updatedParentStage,
            ...(oldStages.slice(parentStageIndex + 1))
        ];

        // "save" our updates to the model
        this.setState({
            stages: updatedStages,
            stageSteps: updatedStageSteps,
            selectedStage: newStage,
            selectedStep: null
        });
    }
    
    openSelectStepDialog() {
        this.setState({showSelectStep: true});
    }

    createStep() {

        const {selectedStage, stageSteps} = this.state;

        if (!selectedStage) {
            return;
        }

        const oldStepsForStage = stageSteps[selectedStage.id] || [];

        let newStep:StepInfo = {
            id: --idSeq,
            isContainer: false,
            children: [],
            type: "script",
            label: "Run Script",
            data: ""
        };

        let newStepsForStage = [...oldStepsForStage, newStep];
        let newStageSteps = {...stageSteps, [selectedStage.id]: newStepsForStage};

        this.setState({stageSteps: newStageSteps, selectedStep: newStep});
    }

    selectedStepChanged(selectedStep:StepInfo) {
        this.setState({selectedStep});
    }

    deleteStep(step:StepInfo) {
        const {selectedStage, stageSteps} = this.state;

        if (!selectedStage) {
            return;
        }

        const oldStepsForStage = stageSteps[selectedStage.id] || [];

        const stepIdx = oldStepsForStage.indexOf(step);

        if (stepIdx < 0) {
            return;
        }

        let newStepsForStage = [
            ...(oldStepsForStage.slice(0, stepIdx)),
            ...(oldStepsForStage.slice(stepIdx + 1))
        ];

        let newSelectedStepIdx = Math.min(stepIdx, newStepsForStage.length - 1);
        let newSelectedStep = newStepsForStage[newSelectedStepIdx];

        let newStageSteps = {...stageSteps, [selectedStage.id]: newStepsForStage};

        this.setState({
            stageSteps: newStageSteps,
            selectedStep: newSelectedStep
        });
    }

    stepDataChanged(newValue:any) {

        const {stageSteps, selectedStage, selectedStep} = this.state;

        if (!stageSteps || !selectedStage || !selectedStep) {
            return;
        }

        const stepsForStage = stageSteps[selectedStage.id];
        const stepIndex = stepsForStage.indexOf(selectedStep);

        let updatedStep = {...selectedStep, data: newValue};
        let updatedStepsForStage = [
            ...(stepsForStage.slice(0, stepIndex)),
            updatedStep,
            ...(stepsForStage.slice(stepIndex + 1))
        ];
        let updatedStageSteps = {...stageSteps, [selectedStage.id]: updatedStepsForStage};

        this.setState({
            stageSteps: updatedStageSteps,
            selectedStep: updatedStep
        });
    }

    deleteStageClicked(e:HTMLEvent) {
        e.target.blur(); // Don't leave ugly selection highlight

        const {selectedStage} = this.state;

        if (selectedStage) {
            this.deleteStage(selectedStage);
        }
    }

    /**
     * Delete the selected stage from our stages list. When this leaves a single-branch of parallel jobs, the steps
     * will be moved to the parent stage, and the lone parallel branch will be deleted.
     *
     * Assumptions:
     *      * The Graph is valid, and contains selectedStage
     *      * Only top-level stages can have children (ie, graph is max depth of 2).
     */
    deleteStage(selectedStage:StageInfo) {

        const {stages, stageSteps} = this.state;
        const parentStage = findParentStage(this.state.stages, selectedStage.id);

        // For simplicity we'll just copy the stages list and then mutate it
        let newStages = [...stages];

        // For the case when we need to move steps from one stage to its parent
        let changedStageSteps = {};

        // We will set this differently depending on our deletion logic
        let newSelectedStage:?StageInfo = null;

        if (parentStage) {
            // Nested stages are a bit more work. First, remove selected stage from parent list

            let newChildren = [...parentStage.children];
            let idx = newChildren.indexOf(selectedStage);
            newChildren.splice(idx, 1);

            // Then check to see if there's more to do
            if (newChildren.length > 1) {
                // Still have multiple parallel branches, so select the next or last child stage
                newSelectedStage = newChildren[Math.min(idx, newChildren.length - 1)];
            } else {
                // We can't have a single parallel stage, so we delete it and move its steps to the parent
                let onlyChild = newChildren[0];
                newChildren = [];
                changedStageSteps[parentStage.id] = stageSteps[onlyChild.id];

                newSelectedStage = null; // Will be set to updated below
            }
            // Update the parent with new children list
            let parentIdx = newStages.indexOf(parentStage);
            let updatedParentStage = {...parentStage, children: newChildren};
            newStages[parentIdx] = updatedParentStage;

            // If we did not select a new child stage above, we'll select updated "parent" that is no longer a parent
            newSelectedStage = newSelectedStage || updatedParentStage;

        } else {
            // Top-level stage is easy :D just delete it...

            let idx = newStages.indexOf(selectedStage);
            newStages.splice(idx, 1);

            // ...then select the next or last one, if there's any left
            let len = newStages.length;
            newSelectedStage = len > 0 ? newStages[Math.min(idx, len - 1)] : null;
        }
        // Copy over steps for all remaining stages
        let newStageSteps:StageStepsMap = {};

        for (let stage of newStages) {
            const id = stage.id;
            newStageSteps[id] = changedStageSteps[id] || stageSteps[id];

            // Assuming children have no children of their own
            for (let childStage of stage.children) {
                const childId = childStage.id;
                newStageSteps[childId] = changedStageSteps[childId] || stageSteps[childId];
            }
        }

        // If we've selected a stage which has children, we need to select its first child instead.
        // Parent stages aren't shown in the graph so aren't selectable; only the name is shown above a column.
        if (newSelectedStage && newSelectedStage.children.length) {
            newSelectedStage = newSelectedStage.children[0];
        }

        // Select the first step of newSelectedStage, if we have one
        let newSelectedStep = null;

        if (newSelectedStage) {
            const stepsForStage = newStageSteps[newSelectedStage.id] || [];
            newSelectedStep = stepsForStage.length ? stepsForStage[0] : null;
        }

        // Update our state
        this.setState({
            stages: newStages,
            stageSteps: newStageSteps,
            selectedStage: newSelectedStage,
            selectedStep: newSelectedStep
        }, () => this.dumpState());
    }

    render() {

        const {stages, selectedStage, selectedStep, s} = this.state;
        const steps = selectedStage ? this.getStepsForStage(selectedStage) : [];

        const title = selectedStage ? selectedStage.name : 'Select or create a pipeline stage';
        const disableIfNoSelection = selectedStage ? {} : {disabled: 'disabled'}; // TODO: Delete if we don't use this any more

        let sv = s ? "visible" : "hidden";

        //console.log("render main"); // TODO: RM
        //console.log("   selectedStage", selectedStage); // TODO: RM

        let detailsOrPlaceholder;

        if (steps && steps.length) {
            detailsOrPlaceholder = (
                <div className="editor-main-stage-details">
                    <div className="editor-main-step-list">
                        {selectedStage ? <EditorStepList steps={steps}
                                                         selectedStep={selectedStep}
                                                         onAddStepClick={() => this.openSelectStepDialog(steps)}
                                                         onStepSelected={(step) => this.selectedStepChanged(step)}
                                                         onDeleteStepClick={(step) => this.deleteStep(step)}/>
                            : <p>Select or create a build stage</p>}
                    </div>
                    <div className="editor-main-step-details">
                        {selectedStage ? <EditorStepDetails step={selectedStep}
                                                            onDataChange={newValue => this.stepDataChanged(newValue)}
                                                            onDeleteStepClick={(step) => this.deleteStep(step)}/>
                            : <p>Select or create a build stage</p>}
                    </div>
                </div>
            );
        } else if (selectedStage) {
            detailsOrPlaceholder = (
                <div className="editor-main-stage-details editor-details-placeholder">
                    <EmptyStateView>
                        <h1>Add a step to <em>{title}</em></h1>

                        <p>
                            Jenkins uses steps within stages to help automate a variety of tasks such as running
                            scripts, checkout out source code and much more.
                        </p>

                        <button onClick={() => this.openSelectStepDialog(selectedStage.steps)}>Add Step</button>
                    </EmptyStateView>
                </div>
            );
        } else {
            detailsOrPlaceholder = (
                <div className="editor-main-stage-details editor-details-placeholder">
                    <EmptyStateView>
                        <h1>{title}</h1>

                        <p>
                            Select a stage on the graph above to reveal steps to be executed in the Pipeline.
                        </p>
                    </EmptyStateView>
                </div>
            );
        }

        let titleBar = selectedStage ? (
            <div className="editor-main-selection-title">
                <h4>{title}</h4>
                <div className="editor-button-bar">
                    <button className="btn-secondary editor-delete-btn"
                        {...disableIfNoSelection}
                            onClick={(e) => this.deleteStageClicked(e)}>
                        Delete stage
                    </button>
                </div>
            </div>
        ) : null;

        return (
            <div className="editor-main">
                <div className="editor-main-graph">
                    <EditorPipelineGraph stages={stages}
                                         selectedStage={selectedStage}
                                         onStageSelected={(stage) => this.graphSelectedStageChanged(stage)}
                                         onCreateSequentialStage={(name) => this.createSequentialStage(name)}
                                         onCreateParallelStage={(name, parentStage) => this.createParallelStage(name, parentStage)}/>
                </div>
                {titleBar}
                {detailsOrPlaceholder}
                <div id="soon" style={{...ss, visibility: sv}} onClick={()=>this.setState({s: false})}>&nbsp;</div>

                {/* <button className="btn-warning" onClick={()=>this.dumpState()}>DUMP</button> */}
                {this.state.showSelectStep && <AddStepSelectionDialog
                    onClose={() => this.setState({showSelectStep: false})}
                    onStepSelected={(step) => { this.setState({showSelectStep: false}); console.log('selected', step); }} />}
            </div>
        );
    }
}

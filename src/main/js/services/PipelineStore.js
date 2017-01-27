// @flow

import idgen from './IdGenerator';
import type { PipelineKeyValuePair } from './PipelineSyntaxConverter';

/**
 * A stage in a pipeline
 */
export type StageInfo = {
    name: string,
    id: number,
    children: Array<StageInfo|UnknownSection>,
    steps: StepInfo[],
    environment: EnvironmentEntryInfo[],
    agent: StepInfo,
};

export type EnvironmentEntryInfo = PipelineKeyValuePair | {
    id: number,
};

/**
 * An individual step within a single pipeline stage
 */
export type StepInfo = {
    id: number,
    name: string,
    label: string,
    isContainer: bool,
    children: StepInfo[],
    data: any,
};

export type PipelineInfo = StageInfo;

export class UnknownSection {
    prop: string;
    json: any;
    constructor(prop: string, json: any) {
        this.prop = prop;
        this.json = json;
    }
}

function _copy<T>(obj: T): ?T {
    if (!obj) {
        return null;
    }
    // TODO: This is awful, use a lib
    return JSON.parse(JSON.stringify(obj));
}

function createBasicStage(name:string):StageInfo {
    return {
        name,
        id: idgen.next(),
        children: [],
        steps: [],
    };
}

/**
 * Search through candidates (and their children, recursively) to see if any is the parent of the stage
 */
function findParentStage(container:StageInfo, childStage:StageInfo, safetyValve:number = 5):?StageInfo {
    // TODO: TESTS
    if (!container || !container.children || container.children.length == 0 || safetyValve < 1) {
        return null;
    }

    for (const child of container.children) {
        if (child.id === childStage.id) {
            return container;
        }

        const foundParent = findParentStage(child, childStage, safetyValve - 1);

        if (foundParent) {
            return foundParent;
        }
    }

    return null;
}

const findStepById = function (steps, id) {
    const step = steps.filter(i => i.id === id);
    if (step.length) {
        return step[0];
    }
    for (let s of steps) {
        if (s.isContainer) {
            const children = s.children;
            if (children) {
                const childStep = findStepById(children, id);
                if (childStep) {
                    return childStep;
                }
            }
        }
    }
};

/**
 * Returns the stage that contains the provided step or undefined
 * if none found
 */
const findStageByStep = function (stage, step) {
    // Does this stage contain this step directly?
    if (stage.steps && stage.steps.length > 0) {
        for (const s of stage.steps) {
            if (s == step) {
                return stage;
            }
        }
        // or is this a nested step?
        const parentStep = findParentStepByChild(stage.steps, step);
        if (parentStep) {
            return stage;
        }
    }

    // try child stages
    if (stage.children && stage.children.length > 0) {
        for (const child of stage.children) {
            const childStage = findStageByStep(child, step);
            if (childStage) {
                return childStage;
            }
        }
    }
};

const findParentStepByChild = function (steps, childStep) {
    for (let s of steps) {
        if (s.isContainer) {
            const children = s.children;
            if (children) {
                for (let c of children) {
                    if (c.id === childStep.id) {
                        return s;
                    }
                }
                const nestedStep = findParentStepByChild(children, childStep);
                if (nestedStep) {
                    return nestedStep;
                }
            }
        }
    }
};

// TODO: mobxify
class PipelineStore {
    pipeline: StageInfo;
    listeners: Function[] = [];

    createSequentialStage(name:string) {
        const { pipeline } = this;

        let newStage = createBasicStage(name);
        const stageId = newStage.id;

        pipeline.children = [...pipeline.children, newStage];
        this.notify();
        return newStage;
    }

    createParallelStage(name:string, parentStage:StageInfo) {
        let updatedChildren = [...parentStage.children]; // Start with a shallow copy, we'll add one or two to this

        let newStage = createBasicStage(name);

        if (parentStage.children.length == 0) {
            // Converting a normal stage with steps into a container of parallel branches, so there's more to do
            let zerothStage = createBasicStage(parentStage.name);

            // Move any steps from the parent stage into the new zeroth stage
            zerothStage.steps = parentStage.steps;
            parentStage.steps = []; // Stages with children can't have steps

            updatedChildren.push(zerothStage);
        }

        updatedChildren.push(newStage); // Add the user's newStage to the parent's child list

        parentStage.children = updatedChildren;
        this.notify();
        return newStage;
    }

    findParentStage(stage: StageInfo) {
        return findParentStage(this.pipeline, stage);
    }

    findStageByStep(step: StepInfo): ?StageInfo {
        const stage = findStageByStep(this.pipeline, step);
        return stage;
    }
    
    findParentStep(childStep: StepInfo): ?StepInfo {
        const stage = findStageByStep(this.pipeline, childStep);
        if (!stage) {
            throw new Error('Stage not found');
        }
        const parent = findParentStepByChild(stage.steps, childStep);
        return parent;
    }
    
    /**
     * Delete the selected stage from our stages list. When this leaves a single-branch of parallel jobs, the steps
     * will be moved to the parent stage, and the lone parallel branch will be deleted.
     *
     * Assumptions:
     *      * The Graph is valid, and contains selectedStage
     *      * Only top-level stages can have children (ie, graph is max depth of 2).
     */
    deleteStage(stage:StageInfo) {
        const parentStage = this.findParentStage(stage) || this.pipeline;

        // For simplicity we'll just copy the stages list and then mutate it
        let newStages = [...parentStage.children];

        // First, remove selected stage from parent list
        let newChildren = [...parentStage.children];
        let idx = newChildren.indexOf(stage);
        newChildren.splice(idx, 1);

        // see if this is a nested stage and we need to move a parallel to a single top-level
        if (parentStage != this.pipeline && newChildren.length === 1) {
            let onlyChild = newChildren[0];
            newChildren = [];
            parentStage.steps = onlyChild.steps;
        }

        // Update the parent with new children list
        parentStage.children = newChildren;

        this.notify();
    }

    addStep(selectedStage: StageInfo, parentStep: StepInfo, step: any): StepInfo {
        if (!selectedStage) {
            throw new Error('Must provide a stage to add steps');
        }

        const oldStepsForStage = selectedStage.steps || [];
        let newStepsForStage = oldStepsForStage;

        let newStep:StepInfo = {
            id: idgen.next(),
            isContainer: step.isBlockContainer,
            children: [],
            name: step.functionName,
            label: step.displayName,
            data: {}
        };

        if (parentStep != null) {
            const parent = findStepById(oldStepsForStage, parentStep.id);
            if (parent) {
                parent.children = parent.children || [];
                parent.children.push(newStep);
            }
            else {
                throw new Error('unable to find step: ' + parentStep.id);
            }
        }
        else {
            newStepsForStage = [...oldStepsForStage, newStep];
        }

        selectedStage.steps = newStepsForStage;
        this.notify();
        return newStep;
    }

    deleteStep(step:StepInfo) {
        const selectedStage = findStageByStep(this.pipeline, step);
        const oldStepsForStage = selectedStage.steps || [];
        let newStepsForStage = oldStepsForStage;
        let newSelectedStep;

        const parent = findParentStepByChild(selectedStage.steps, step);
        if (parent) {
            const stepIdx = parent.children.indexOf(step);

            if (stepIdx < 0) {
                return;
            }

            parent.children = [
                ...(parent.children.slice(0, stepIdx)),
                ...(parent.children.slice(stepIdx + 1))
            ];
            
            newSelectedStep = parent;
        }
        else { // no parent
            const stepIdx = oldStepsForStage.indexOf(step);

            if (stepIdx < 0) {
                return;
            }

            selectedStage.steps = [
                ...(oldStepsForStage.slice(0, stepIdx)),
                ...(oldStepsForStage.slice(stepIdx + 1))
            ];

            let newSelectedStepIdx = Math.min(stepIdx, newStepsForStage.length - 1);
            newSelectedStep = newStepsForStage[newSelectedStepIdx];
        }
        this.notify();
    }

    setPipeline(pipeline: PipelineInfo) {
        this.pipeline = pipeline;
        this.notify();
    }

    notify() {
        this.listeners.map(l => l());
    }

    addListener(fn: Function) {
        this.listeners.push(fn);
    }

    removeListener(fn: Function) {
        const idx = this.listeners.indexOf(fn);
        this.listeners.splice(idx, 1);
    }
}

const pipelineStore = new PipelineStore();

export default pipelineStore;

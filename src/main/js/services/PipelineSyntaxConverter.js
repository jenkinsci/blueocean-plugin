// @flow

import fetch from './fetchClassic';
import { UnknownSection } from './PipelineStore';
import type { PipelineInfo, StageInfo, StepInfo } from './PipelineStore';
import pipelineMetadataService from './PipelineMetadataService';
import idgen from './IdGenerator';

const value = 'value';

export type PipelineJsonContainer = {
    pipeline: PipelineJson,
};

export type PipelineAgent = {
    type: string,
    arguments: PipelineNamedValueDescriptor[],
};

export type PipelineJson = {
    stages: PipelineStage[],
    agent: PipelineAgent,
};

export type PipelineValueDescriptor = {
    isLiteral: boolean,
    value: string,
};

export type PipelineNamedValueDescriptor = {
    key: string,
    value: PipelineValueDescriptor,
};

export type PipelineStep = {
    name: string,
    children?: PipelineStep[],
    arguments: PipelineValueDescriptor | PipelineNamedValueDescriptor[],
};

export type PipelineStage = {
    name: string,
    branches?: PipelineStage[],
    agent?: PipelineValueDescriptor[],
    steps?: PipelineStep[],
    environment?: PipelineNamedValueDescriptor[],
};

function singleValue(v: any) {
    if (Array.isArray(v)) {
        return v[0];
    }
    return {
        value: v,
    };
}

function clone<T>(v: T): T {
    return JSON.parse(JSON.stringify(v));
}

function captureUnknownSections(pipeline: any, internal: any, ...knownSections: string[]) {
    for (const prop of Object.keys(pipeline)) {
        if (knownSections.indexOf(prop) >= 0) {
            continue;
        }
        internal[prop] = new UnknownSection(prop, pipeline[prop]);
    }
}

function restoreUnknownSections(internal: any, out: any) {
    for (const prop of Object.keys(internal)) {
        const val = internal[prop];
        if (val instanceof UnknownSection) {
            out[val.prop] = val.json;
        }
    }
}

function removeExtraMarkers(list: any): any {
    if (list instanceof Object) {
        if (!list.map) {
            const v = clone(list);
            // Get rid of the extra UI markers
            delete v.id;
            delete v.pristine;
            delete v.validationErrors;
            return v;
        }
            
        return list.map(o => {
            const v = clone(o);
            // Get rid of the extra UI markers
            delete v.id;
            delete v.pristine;
            delete v.validationErrors;
            return v;
        });
    }
    return list;
}

function convertEnvironmentToInternal(environment: any[]): any[] {
    return !environment ? [] : environment.map(o => {
        o.id = idgen.next();
        return o;
    });
}

export function convertJsonToInternalModel(json: PipelineJsonContainer): PipelineInfo {
    const pipeline = json.pipeline;
    const out: PipelineInfo = {
        id: idgen.next(),
        children: [],
        steps: [],
    };

    if (!pipeline.agent) {
        // we default agent to 'any'
        out.agent = { type: 'any' };
    } else {
        out.agent = pipeline.agent;
    }

    out.environment = convertEnvironmentToInternal(pipeline.environment);

    if (!pipeline.stages) {
        throw new Error('Pipeline must define stages');
    }

    // capture unknown sections
    captureUnknownSections(pipeline, out, 'agent', 'stages', 'environment');

    for (let i = 0; i < pipeline.stages.length; i++) {
        const topStage = pipeline.stages[i];

        const topStageInfo: StageInfo = {
            id: idgen.next(),
            name: topStage.name,
            children: [],
            steps: [],
        };

        // FIXME: this is per top-level stage, only...
        topStageInfo.agent = topStage.agent;
        topStageInfo.environment = convertEnvironmentToInternal(topStage.environment);

        captureUnknownSections(topStage, topStageInfo, 'name', 'steps', 'environment', 'agent');

        out.children.push(topStageInfo);

        for (let j = 0; j < topStage.branches.length; j++) {
            const b = topStage.branches[j];

            let stage: StageInfo;
            if (b.name == 'default' && topStage.branches.length === 1) {
                // non-parallel top-level stages are defined by a single
                // nested stage named 'default'
                stage = topStageInfo;
            } else {
                // Otherwise this is part of a parallel set
                stage = {
                    id: idgen.next(),
                    name: b.name,
                    children: [],
                    steps: [],
                };
                topStageInfo.children.push(stage);
            }

            for (let stepIndex = 0; stepIndex < b.steps.length; stepIndex++) {
                const s = b.steps[stepIndex];
                const step = convertStepFromJson(s);
                stage.steps.push(step);
            }
        }
    }

    return out;
}

export function convertStepFromJson(s: PipelineStep) {
    // this will already have been called and cached:
    let stepMeta = [];
    pipelineMetadataService.getStepListing(steps => {
        stepMeta = steps;
    });
    const meta = stepMeta.filter(md => md.functionName === s.name)[0]
    
    // handle unknown steps
    || {
        isBlockContainer: false,
        displayName: s.name,
    };

    const step = {
        name: s.name,
        label: meta.displayName,
        data: {},
        isContainer: meta.isBlockContainer,
        children: [],
        id: idgen.next(),
    };
    if (s.arguments) {
        const args = s.arguments instanceof Array ? s.arguments : [ s.arguments ];
        for (let k = 0; k < args.length; k++) {
            const arg = args[k];
            if (arg.key) {
                step.data[arg.key] = arg.value.value;
            } else {
                if (!meta.parameters) {
                    throw new Error('No parameters for: ' + s.name);
                }
                // this must be a requiredSingleParameter,
                // need to find it to set the right parameter value
                const param = meta.parameters.filter(a => a.isRequired)[0];
                if (!param) {
                    throw new Error('Unable to find required parameter for: ' + s.name);
                }
                step.data[param.name] = arg.value;
            }
        }
    }
    if (s.children && s.children.length > 0) {
        for (const c of s.children) {
            const child = convertStepFromJson(c);
            step.children.push(child);
        }
    }
    return step;
}

function _lit(value: any): PipelineValueDescriptor {
    if (value instanceof Object) {
        if ('isLiteral' in value) {
            return value;
        }
        if ('value' in value) {
            return _lit(value.value);
        }
    }
    return {
        isLiteral: true,
        value: value,
    };
}

function _convertStepArguments(step: StepInfo): PipelineNamedValueDescriptor[] {
    const out: PipelineNamedValueDescriptor[] = [];
    for (const arg of Object.keys(step.data)) {
        const val = step.data[arg];
        if (val) {
            out.push({
                key: arg,
                value: _lit(val),
            });
        }
    }
    return out;
}

export function convertStepsToJson(steps: StepInfo[]): PipelineStep[] {
    const out: PipelineStep[] = [];
    for (const step of steps) {
        const s: PipelineStep = {
            name: step.name,
            arguments: _convertStepArguments(step),
        };
        if (step.children && step.children.length > 0) {
            s.children = convertStepsToJson(step.children);
        }
        out.push(s);
    }
    return out;
}

export function convertStageToJson(stage: StageInfo): PipelineStage {
    const out: PipelineStage = {
        name: stage.name,
    };

    // FIXME this is going to have to change, there's currently no way to define
    // an agent for each parallel branch, with nested stages and/or execution
    // graph order, this will go away in favor of a different mechanism...
    if (stage.agent && stage.agent && stage.agent.type != 'none') {
        out.agent = removeExtraMarkers(stage.agent);
    }

    if (stage.environment && stage.environment.length) {
        out.environment = removeExtraMarkers(stage.environment);
    }

    restoreUnknownSections(stage, out);

    if (stage.children && stage.children.length > 0) {
        // parallel
        out.branches = [];

        // TODO Currently, sub-stages are not supported, this should be recursive
        for (const child of stage.children) {
            const outStage: PipelineStage = {
                name: child.name,
                steps: convertStepsToJson(child.steps),
            };

            out.branches.push(outStage);
        }
    } else {
        // single, add a 'default' branch
        const outBranch = {
            name: 'default',
            steps: convertStepsToJson(stage.steps),
        };

        out.branches = [ outBranch ];
    }

    return out;
}

export function convertInternalModelToJson(pipeline: PipelineInfo): PipelineJsonContainer {
    const out: PipelineJsonContainer = {
        pipeline: {
            agent: removeExtraMarkers(pipeline.agent),
            stages: [],
        },
    };
    const outPipeline = out.pipeline;

    if (pipeline.environment && pipeline.environment.length) {
        outPipeline.environment = removeExtraMarkers(pipeline.environment);
    }

    restoreUnknownSections(pipeline, outPipeline);

    for (const stage of pipeline.children) {
        const s = convertStageToJson(stage);
        outPipeline.stages.push(s);
    }
    return out;
}
export function convertPipelineToJson(pipeline: string, handler: Function) {
    pipelineMetadataService.getStepListing(steps => {
        fetch('/pipeline-model-converter/toJson',
            'jenkinsfile=' + encodeURIComponent(pipeline), data => {
                if (data.errors) {
                    if (window.isDevelopmentMode) console.error(data);
                }
                handler(data.json, data.errors);
            });
    });
}

export function convertJsonToPipeline(json: string, handler: Function) {
    pipelineMetadataService.getStepListing(steps => {
        fetch('/pipeline-model-converter/toJenkinsfile',
            'json=' + encodeURIComponent(json), data => {
                if (data.errors) {
                    if (window.isDevelopmentMode) console.error(data);
                }
                handler(data.jenkinsfile, data.errors);
            });
    });
}

export function convertPipelineStepsToJson(pipeline: string, handler: Function) {
    pipelineMetadataService.getStepListing(steps => {
        fetch('/pipeline-model-converter/stepsToJson',
            'jenkinsfile=' + encodeURIComponent(pipeline), data => {
                if (data.errors) {
                    if (window.isDevelopmentMode) console.error(data);
                }
                handler(data.json, data.errors);
            });
    });
}

export function convertJsonStepsToPipeline(step: PipelineStep, handler: Function) {
    pipelineMetadataService.getStepListing(steps => {
        fetch('/pipeline-model-converter/stepsToJenkinsfile',
            'json=' + encodeURIComponent(JSON.stringify(step)), data => {
                if (data.errors) {
                    if (window.isDevelopmentMode) console.error(data);
                }
                handler(data.jenkinsfile, data.errors);
            });
    });
}

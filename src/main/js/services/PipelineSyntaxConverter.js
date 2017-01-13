// @flow

import { Fetch, UrlConfig } from '@jenkins-cd/blueocean-core-js';
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
    environment: PipelineEnvironment,
};

export type PipelineEnvironment = {
    environment: Map<String,String>,
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
    environment?: PipelineEnvironment,
};

function singleValue(v: any) {
    if (Array.isArray(v)) {
        return v[0];
    }
    return {
        value: v,
    };
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

export function convertJsonToInternalModel(json: PipelineJsonContainer): PipelineInfo {
    const pipeline = json.pipeline;
    const out: PipelineInfo = {
        id: idgen.next(),
        children: [],
        steps: [],
    };

    if (!pipeline.agent) {
        // we default agent to 'any'
        out.agent = {
            value: {
                value: 'any',
            },
        };
    } else {
        out.agent = pipeline.agent;
    }

    out.environment = pipeline.environment;

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
        topStageInfo.environment = topStage.environment;

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

            captureUnknownSections(b, stage, 'name', 'steps', 'environment');
    
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
        out.push({
            key: arg,
            value: _lit(step.data[arg]),
        });
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
        out.agent = stage.agent;
    }

    if (stage.environment && stage.environment.length && stage.environment[0].key != 'none') {
        out.environment = stage.environment;
    }

    if (stage.children && stage.children.length > 0) {
        // parallel
        out.branches = [];

        // TODO Currently, sub-stages are not supported, this should be recursive
        for (const child of stage.children) {
            const outStage: PipelineStage = {
                name: child.name,
                steps: convertStepsToJson(child.steps),
            };

            restoreUnknownSections(child, outStage);

            out.branches.push(outStage);
        }
    } else {
        // single, add a 'default' branch
        const outBranch = {
            name: 'default',
            steps: convertStepsToJson(stage.steps),
        };

        out.branches = [ outBranch ];

        restoreUnknownSections(stage, outBranch);
    }

    return out;
}

export function convertInternalModelToJson(pipeline: PipelineInfo): PipelineJsonContainer {
    const out: PipelineJsonContainer = {
        pipeline: {
            agent: pipeline.agent,
            stages: [],
        },
    };
    const outPipeline = out.pipeline;

    if (pipeline.environment && pipeline.environment.length) {
        outPipeline.environment = pipeline.environment;
    }

    restoreUnknownSections(pipeline, outPipeline);

    for (const stage of pipeline.children) {
        const s = convertStageToJson(stage);
        outPipeline.stages.push(s);
    }
    return out;
}

function fetch(url, body, handler) {
    Fetch.fetch(`${UrlConfig.getJenkinsRootURL()}/blue/rest/pipeline-metadata/crumbInfo`, {
        fetchOptions: { method: 'GET' }
    }).then(response => {
        if (!response.ok) {
            console.log('An error happened fetching ', url);
            return;
        }
        const useCrumb = function (crumb) {
            crumb = crumb.split('=');
            const headers = {
                'Content-Type': 'application/x-www-form-urlencoded'
            };
            headers[crumb[0]] = crumb[1];
            Fetch.fetchJSON(url, {
                fetchOptions: {
                    method: 'POST',
                    body: body,
                    headers: headers,
                }
            }).then(data => {
                if (data.status === 'ok') {
                    handler(data.data);
                } else {
                    console.log(data);
                }
            });
        };
        let crumb = response.text();
        if (crumb instanceof Promise) {
            crumb.then(useCrumb);
        } else {
            useCrumb(crumb);
        }
    });
}

export function convertPipelineToJson(pipeline: string, handler: Function) {
    pipelineMetadataService.getStepListing(steps => {
        fetch(`${UrlConfig.getJenkinsRootURL()}/pipeline-model-converter/toJson`,
            'jenkinsfile=' + encodeURIComponent(pipeline), data => {
                if (data.errors) {
                    console.log(data);
                }
                handler(data.json, data.errors);
            });
    });
}

export function convertJsonToPipeline(json: string, handler: Function) {
    pipelineMetadataService.getStepListing(steps => {
        fetch(`${UrlConfig.getJenkinsRootURL()}/pipeline-model-converter/toJenkinsfile`,
            'json=' + encodeURIComponent(json), data => {
                if (data.errors) {
                    console.log(data);
                }
                handler(data.jenkinsfile, data.errors);
            });
    });
}

export function convertPipelineStepsToJson(pipeline: string, handler: Function) {
    pipelineMetadataService.getStepListing(steps => {
        fetch(`${UrlConfig.getJenkinsRootURL()}/pipeline-model-converter/stepsToJson`,
            'jenkinsfile=' + encodeURIComponent(pipeline), data => {
                if (data.errors) {
                    console.log(data);
                }
                handler(data.json, data.errors);
            });
    });
}

export function convertJsonStepsToPipeline(step: PipelineStep, handler: Function) {
    pipelineMetadataService.getStepListing(steps => {
        fetch(`${UrlConfig.getJenkinsRootURL()}/pipeline-model-converter/stepsToJenkinsfile`,
            'json=' + encodeURIComponent(JSON.stringify(step)), data => {
                if (data.errors) {
                    console.log(data);
                }
                handler(data.jenkinsfile, data.errors);
            });
    });
}

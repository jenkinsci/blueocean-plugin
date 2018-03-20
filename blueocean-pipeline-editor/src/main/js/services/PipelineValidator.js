// @flow

import fetch from './fetchClassic';
import pipelineStore from './PipelineStore';
import pipelineMetadataService from './PipelineMetadataService';
import type { PipelineInfo, StageInfo, StepInfo } from './PipelineStore';
import { convertInternalModelToJson } from './PipelineSyntaxConverter';
import { isObservableArray } from 'mobx';
import debounce from 'lodash.debounce';

const validationTimeout = 500;

export function isValidEnvironmentKey(key: string): boolean {
    if (!key) {
        return false;
    }
    if (/^[_$a-zA-Z\xA0-\uFFFF][_$a-zA-Z0-9\xA0-\uFFFF]*$/.test(key)) {
        return true;
    }
    return false;
}

function _isArray(o) {
    return o instanceof Array || typeof o === 'array' || isObservableArray(o);
}

function _hasValidationErrors(node) {
    return node && node.validationErrors && node.validationErrors.length;
}

function _appendValidationError(node, message) {
    if (_hasValidationErrors(node)) {
        node.validationErrors.push(message);
    } else {
        node.validationErrors = [message];
    }
}

function _validateAgentEntries(metadata, agent) {
    if (!agent || agent.type === 'none' || agent.type === 'any') {
        return;
    }
    let meta;
    for (const m of metadata.agentMetadata) {
        if (agent.type === m.symbol) {
            meta = m;
            break;
        }
    }
    if (!meta) {
        _appendValidationError(meta, 'Unknown agent type: ' + agent.type);
        return;
    }
    meta.parameters.map(param => {
        const arg = agent.arguments.filter(arg => arg.key === param.name)[0];
        if (param.isRequired && (!arg || !arg.value || !arg.value.value)) {
            _appendValidationError(agent, param.name + ' is required');
        }
    });
}

function _validateEnvironmentEntries(metadata, entries) {
    for (const entry of entries) {
        if (!_hasValidationErrors(entry) && !isValidEnvironmentKey(entry.key)) {
            _appendValidationError(entry, 'Environment Name is not valid. Please ensure it is a valid Pipeline identifier.');
        }
    }
}

function _validateStepValues(metadata, steps) {
    for (const step of steps) {
        const meta = metadata.stepMetadata.find(step);
        if (meta && meta.isRequired && !step.validationErrors && !step.value.value) {
            _appendValidationError(entry, 'Required step value');
        }
        if (step.children) {
            _validateStepValues(metadata, step.children);
        }
    }
}

function _addClientSideErrors(metadata, node) {
    const parent = pipelineStore.findParentStage(node);
    if (node.agent) {
        _validateAgentEntries(metadata, node.agent);
    }
    if (node.environment) {
        _validateEnvironmentEntries(metadata, node.environment);
    }
    if (node.steps) {
        _validateStepValues(metadata, node.steps);
    }
    if (!node.children || !node.children.length) {
        if (parent && (!node.steps || !node.steps.length)) {
            // For this one particular error, just replace it
            //node.validationErrors = [];
            //node.steps.validationErrors = [ 'At least one step is required' ];
            node.validationErrors = ['At least one step is required'];
        }
        if (node === pipelineStore.pipeline) {
            // override default message
            node.validationErrors = ['A stage is required'];
        }
    } else {
        node.children.map(child => _addClientSideErrors(metadata, child));
    }
}

export class PipelineValidator {
    lastPipelineValidated: string;

    validatePipeline(pipeline: PipelineInfo, handler: ValidationResult) {
        const json = convertInternalModelToJson(pipeline);
        fetch(
            '/pipeline-model-converter/validateJson',
            'json=' + encodeURIComponent(JSON.stringify(json)),
            data => {
                if (!data.result && data.errors) {
                    if (window.isDevelopmentMode) console.error(data);
                }
                handler(data);
            },
            { disableLoadingIndicator: true }
        );
    }

    /**
     * Indicates this node or any child node has a validation error
     */
    hasValidationErrors(node: Object, visited: any[] = []): boolean {
        if (visited.indexOf(node) >= 0) {
            return false;
        }
        visited.push(node);
        if (_hasValidationErrors(node)) {
            return true;
        }
        // if this is a parallel, check the parent stage for errors
        const parent = pipelineStore.findParentStage(node);
        if (parent && pipelineStore.pipeline !== parent && parent.validationErrors) {
            return true;
        }

        for (const key of Object.keys(node)) {
            const val = node[key];
            if (val instanceof Object) {
                if (this.hasValidationErrors(val, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the validation errors for the specific node
     */
    getNodeValidationErrors(node: Object, visited: any[] = []): Object[] {
        const validationErrors = node.validationErrors ? [...node.validationErrors] : [];

        // if this is a parallel, check the parent stage for errors
        const parent = pipelineStore.findParentStage(node);
        if (parent && pipelineStore.pipeline !== parent && parent.validationErrors) {
            validationErrors.push.apply(validationErrors, parent.validationErrors);
        }

        return validationErrors.length ? validationErrors : null;
    }

    /**
     * Gets all validation errors for the node and all child nodes
     */
    getAllValidationErrors(node: Object, visited: any[] = []): Object[] {
        if (visited.indexOf(node) >= 0) {
            return null;
        }
        visited.push(node);
        const validationErrors = [];
        if (_hasValidationErrors(node)) {
            validationErrors.push.apply(validationErrors, node.validationErrors);
        }
        if (node instanceof Array || typeof node === 'array') {
            for (const v of node) {
                const childErrors = this.getAllValidationErrors(v, visited);
                if (childErrors) {
                    validationErrors.push.apply(validationErrors, childErrors);
                }
            }
        } else if (node instanceof Object) {
            for (const key of Object.keys(node)) {
                const childErrors = this.getAllValidationErrors(node[key], visited);
                if (childErrors) {
                    validationErrors.push.apply(validationErrors, childErrors);
                }
            }
        }
        return validationErrors.length ? validationErrors : null;
    }

    findNodeFromPath(pipeline: PipelineInfo, path: string[]): any {
        // like: "pipeline"/"stages"/"0"/"branches"/"0"/"steps"
        let node = pipeline;
        for (let i = 0; i < path.length; i++) {
            const part = path[i];
            switch (part) {
                case 'pipeline': {
                    break;
                }
                case 'stages': {
                    const idx = parseInt(path[++i]);
                    if (parseInt(idx) === idx) {
                        node = node.children[idx];
                    }
                    break;
                }
                case 'branches': {
                    const idx = parseInt(path[++i]);
                    // check if the 'default' single node path vs. parallel
                    if (!node.children || node.children.length == 0) {
                        // This probably in a parallel block, and is referencing the default branch, which is this node
                    } else {
                        node = node.children[idx];
                    }
                    break;
                }
                case 'parallel': {
                    const idx = parseInt(path[++i]);
                    if (!isNaN(idx)) {
                        node = node.children[idx];
                    }
                    break;
                }
                case 'steps': {
                    const idx = parseInt(path[++i]);
                    if (!isNaN(idx)) {
                        // it is actually the steps array, so just target the node
                        node = node.steps[idx];
                    }
                    break;
                }
                case 'arguments': {
                    const idx = parseInt(path[++i]);
                    if (!isNaN(idx)) {
                        // it is actually the arguments array, so just target the node
                        // FIXME ehh, arguments are stored in 'data'
                        node = node.data;
                    }
                    break;
                }
                default: {
                    // if we have reached a key/value, just apply the error here
                    if (node && 'key' in node && 'value' in node) {
                        return node;
                    }
                    // some error with some unknown section, try to find it
                    // so we can at least display the error
                    node = node[part];
                    break;
                }
            }
        }
        if (!node) {
            if (window.isDevelopmentMode) console.error('unable to find node for', path, 'in', pipeline);
            return pipeline;
        }
        return node;
    }

    applyValidationMarkers(metadata, pipeline: PipelineInfo, validation: Object): void {
        // just make sure nothing is hanging around
        this.clearValidationMarkers(pipeline);
        if (validation.result == 'failure') {
            for (const error of validation.errors) {
                if (error.location) {
                    const node = this.findNodeFromPath(pipeline, error.location);
                    if (node) {
                        // ignore errors for nodes that are 'pristine'
                        if (!node.pristine) {
                            _appendValidationError(node, error.error);
                        }
                        error.applied = true;
                    }
                } else if (error.jenkinsfileErrors) {
                    for (const globalError of error.jenkinsfileErrors.errors) {
                        if (_isArray(globalError.error)) {
                            for (const errorText of globalError.error) {
                                _appendValidationError(pipeline, errorText);
                                error.applied = true;
                            }
                        } else {
                            _appendValidationError(pipeline, globalError.error);
                            error.applied = true;
                        }
                    }
                }
            }
            for (const error of validation.errors) {
                if (!error.applied) {
                    if (window.developmentMode) console.error(error);
                    // surface in the UI
                    _appendValidationError(pipeline, error.error);
                }
            }
        }
        _addClientSideErrors(metadata, pipeline);
    }

    clearValidationMarkers(node: Object, visited: any[] = []): void {
        if (visited.indexOf(node) >= 0) {
            return;
        }
        visited.push(node);
        if (node.validationErrors) {
            delete node.validationErrors;
        }
        if (_isArray(node)) {
            for (const v of node) {
                this.clearValidationMarkers(v, visited);
            }
        } else if (node instanceof Object) {
            for (const key of Object.keys(node)) {
                const val = node[key];
                if (val instanceof Object) {
                    this.clearValidationMarkers(val, visited);
                }
            }
        }
    }

    hasPristineEdits(node: Object, visited: any[] = []) {
        if (visited.indexOf(node) >= 0) {
            return false;
        }
        visited.push(node);
        if (node.pristine) {
            return true;
        }
        for (const key of Object.keys(node)) {
            const val = node[key];
            if (val instanceof Object) {
                if (this.hasPristineEdits(val, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    validateNow(onComplete) {
        const pipeline = pipelineStore.pipeline;
        const json = JSON.stringify(convertInternalModelToJson(pipeline));
        this.lastPipelineValidated = json + (this.hasPristineEdits(pipeline) ? '.' : '');
        pipelineMetadataService.getStepListing(stepMetadata => {
            pipelineMetadataService.getAgentListing(agentMetadata => {
                this.validatePipeline(pipeline, validationResult => {
                    this.applyValidationMarkers({ stepMetadata, agentMetadata }, pipeline, validationResult);
                    pipelineStore.setPipeline(pipeline); // notify listeners to re-render
                    if (onComplete) onComplete();
                });
            });
        });
    }

    delayedValidate = debounce(() => {
        this.validateNow();
    }, validationTimeout);

    validate(onComplete) {
        const json = JSON.stringify(convertInternalModelToJson(pipelineStore.pipeline));
        if (this.lastPipelineValidated === json) {
            if (onComplete) onComplete();
            return;
        }
        if (!this.lastPipelineValidate) {
            this.validateNow(onComplete);
        } else {
            this.delayedValidate();
        }
    }
}

const pipelineValidator = new PipelineValidator();

export default pipelineValidator;

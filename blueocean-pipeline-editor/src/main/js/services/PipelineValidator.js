// @flow

import fetch from './fetchClassic';
import pipelineStore from './PipelineStore';
import type { PipelineInfo, StageInfo, StepInfo } from './PipelineStore';
import { convertInternalModelToJson } from './PipelineSyntaxConverter';
import idgen from './IdGenerator';
import debounce from 'lodash.debounce';

const validationTimeout = 500;

function _addErrorsForStagesWithoutSteps(node) {
    const parent = pipelineStore.findParentStage(node);
    if (!node.children || !node.children.length) {
        if (parent && (!node.steps || !node.steps.length)) {
            const message = 'At least one step is required';
            if (node.validationErrors) {
                node.validationErrors[0] = message;
            } else {
                node.validationErrors = [ message ];
            }
        }
    } else {
        node.children.map(child => _addErrorsForStagesWithoutSteps(child));
    }
}

export class PipelineValidator {
    lastPipelineValidated: string;

    validatePipeline(pipeline: PipelineInfo, handler: ValidationResult) {
        const json = convertInternalModelToJson(pipeline);
        fetch('/pipeline-model-converter/validateJson',
        'json=' + encodeURIComponent(JSON.stringify(json)), data => {
            if (!data.result && data.errors) {
                if (window.isDevelopmentMode) console.error(data);
            }
            handler(data);
        }, { disableLoadingIndicator: true });
    }

    /**
     * Indicates this node or any child node has a validation error
     */
    hasValidationErrors(node: Object, visited: any[] = []): boolean {
        if (visited.indexOf(node) >= 0) {
            return false;
        }
        visited.push(node);
        if (node.validationErrors) {
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
                if(this.hasValidationErrors(val, visited)) {
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
        const validationErrors = node.validationErrors ? [ ...node.validationErrors ] : [];
        
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
        if (node.validationErrors) {
            validationErrors.push.apply(validationErrors, node.validationErrors);
        }
        for (const key of Object.keys(node)) {
            const val = node[key];
            if (val instanceof Object) {
                const childErrors = this.getAllValidationErrors(val, visited);
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
            switch(part) {
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
                        // err.. nothing to do
                    } else {
                        node = node.children[idx];
                    }
                    break;
                }
                case 'steps': {
                    const idx = parseInt(path[++i]);
                    if (!isNaN(idx)) { // it is actually the steps array, so just target the node
                        node = node.steps[idx];
                    }
                    break;
                }
                case 'arguments': {
                    const idx = parseInt(path[++i]);
                    if (!isNaN(idx)) { // it is actually the arguments array, so just target the node
                        // FIXME ehh, arguments are stored in 'data'
                        node = node.data;
                    }
                    break;
                }
                default: {
                    // some error with some unknown section, try to find it
                    // so we can at least display the error
                    node = node[part];
                    break;
                }
            }
        }
        if (!node) {
            if (window.isDevelopmentMode) console.error('unable to find node for', path, 'in', pipeline);
        }
        return node;
    }

    applyValidationMarkers(pipeline: PipelineInfo, validation: Object): void {
        // just make sure nothing is hanging around
        this.clearValidationMarkers(pipeline);
        if (validation.result == 'failure') {
            for (const error of validation.errors) {
                const node = this.findNodeFromPath(pipeline, error.location);
                if (node && !node.pristine) {
                    if (!node.validationErrors) {
                        node.validationErrors = [ error.error ];
                    } else {
                        node.validationErrors.push(error.error);
                    }
                }
            }
        }

        _addErrorsForStagesWithoutSteps(pipeline);
    }

    clearValidationMarkers(node: Object, visited: any[] = []): void {
        if (visited.indexOf(node) >= 0) {
            return;
        }
        visited.push(node);
        if (node.validationErrors) {
            delete node.validationErrors;
        }
        for (const key of Object.keys(node)) {
            const val = node[key];
            if (val instanceof Object) {
                this.clearValidationMarkers(val, visited);
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
                if(this.hasPristineEdits(val, visited)) {
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
        this.validatePipeline(pipeline, validationResult => {
            this.applyValidationMarkers(pipeline, validationResult);
            pipelineStore.setPipeline(pipeline); // notify listeners to re-render
            if (onComplete) onComplete();
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

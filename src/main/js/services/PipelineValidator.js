// @flow

import fetch from './fetchClassic';
import pipelineStore from './PipelineStore';
import type { PipelineInfo, StageInfo, StepInfo } from './PipelineStore';
import { convertInternalModelToJson } from './PipelineSyntaxConverter';
import pipelineMetadataService from './PipelineMetadataService';
import idgen from './IdGenerator';
import debounce from 'lodash.debounce';

const validationTimeout = 500;

export class PipelineValidator {
    lastPipelineValidated: string;

    validatePipeline(pipeline: PipelineInfo, handler: ValidationResult) {
        const json = convertInternalModelToJson(pipeline);
        fetch('/pipeline-model-converter/validateJson',
        'json=' + encodeURIComponent(JSON.stringify(json)), data => {
            if (!data.result && data.errors) {
                console.error(data);
            }
            handler(data);
        });
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
                    node = node.children[idx];
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
            console.error('unable to find node for', path, 'in', pipeline);
        }
        return node;
    }

    applyValidationMarkers(pipeline: PipelineInfo, validation: Object): void {
        // just make sure nothing is hanging around
        this.clearValidationMarkers(pipeline);
        if (validation.result == 'failure') {
            for (const error of validation.errors) {
                const node = this.findNodeFromPath(pipeline, error.location);
                if (node) {
                    if (!node.validationErrors) {
                        node.validationErrors = [ error.error ];
                    } else {
                        node.validationErrors.push(error.error);
                    }
                }
            }
        }
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

    validateNow() {
        const pipeline = pipelineStore.pipeline;
        const json = JSON.stringify(convertInternalModelToJson(pipeline));
        this.lastPipelineValidated = json;
        this.validatePipeline(pipeline, validationResult => {
            this.applyValidationMarkers(pipeline, validationResult);
            pipelineStore.setPipeline(pipeline); // notify listeners to re-render
        });
    }

    delayedValidate = debounce(() => {
        this.validateNow();
    }, validationTimeout);

    validate() {
        const json = JSON.stringify(convertInternalModelToJson(pipelineStore.pipeline));
        if (this.lastPipelineValidated === json) {
            return;
        }
        if (!this.lastPipelineValidate) {
            this.validateNow();
        } else {
            this.delayedValidate();
        }
    }
}

const pipelineValidator = new PipelineValidator();

export default pipelineValidator;

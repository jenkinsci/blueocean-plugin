import { action, asFlat, computed, observable } from 'mobx';
import { logging, Utils } from '@jenkins-cd/blueocean-core-js';


const LOGGER = logging.logger('io.jenkins.blueocean.create-pipeline');


/**
 * Base class for managing the flow of multiple steps.
 * Must provide an initial step, and has methods for pushing or replacing steps on stack.
 */
export default class FlowManager {
    get redirectTimeout() {
        return 2000;
    }

    @computed
    get activeIndex() {
        return this.steps.length > 0 ?
            this.steps.length - 1 : 0;
    }

    /**
     * Sets up the initial state of the flow. Do not override.
     * @param listener
     */
    initialize(listener) {
        this._reset();

        this.listener = listener;

        this._setupStates();
        this._setupInitialStep();
        this.onInitialized();
    }

    getStates() {
        this._throwError('needs valid state list');
    }

    /**
     * Return a React component (enclosed by FlowStep) that starts the flow
     */
    getInitialStep() {
        this._throwError('need initial step');
    }

    /**
     * Callback invoked after initial step is set up
     */
    onInitialized() {}

    // new APIS

    @observable
    stateId = null;

    states = [];

    steps = [];

    placeholders = [];


    /**
     * Render the specified state and step.
     *
     * @param stateId
     * @param stepElement
     * @param afterStateId
     */
    @action
    renderStep({ stateId, stepElement, afterStateId = null }) {
        if (!stateId || !stepElement) {
            this._throwError('stateId and stepElement are required');
        }

        if (this.states.indexOf(stateId) === -1) {
            this._throwError(`stateId=${stateId} is not defined in states`);
        }

        if (afterStateId && !this.isStateAdded(afterStateId)) {
            this._throwError(`cannot find afterStateId=${afterStateId}`);
        }

        const newStep = this._createStep(stateId, stepElement);

        if (this.isStateAdded(stateId)) {
            this._replaceStep(newStep, stateId);
        } else {
            if (!afterStateId && this.steps.length === 0) {
                this.steps.push(newStep);
            } else if (!afterStateId && this.steps.length > 0) {
                this.steps.replace([newStep]);
            } else {
                this._addStepAfter(newStep, afterStateId);
            }
        }

        this.changeState(stateId);
    }

    /**
     * Removes any steps after the specified state id.
     * @param afterStateId
     */
    @action
    removeSteps({ afterStateId }) {
        if (!afterStateId) {
            this._throwError('afterStateId is required');
        }

        // lose any steps after the current step
        const stepsCopy = this._sliceSteps(afterStateId);
        this.steps.replace(stepsCopy);

        // update the current stateId to match the last step's stateId
        const lastStep = this.steps[this.steps.length - 1];
        this.changeState(lastStep.stateId);
    }

    _createStep(stateId, stepElement) {
        const newStep = {
            stateId,
            stepElement,
        };

        // each time a new step instance is created we want fresh React state
        // assign a unique ID to the React element's key to force a remount
        newStep.stepElement.key = Utils.randomId();
        return newStep;
    }

    @action
    _addStepAfter(newStep, afterStateId) {
        const stepsCopy = this._sliceSteps(afterStateId);
        stepsCopy.push(newStep);
        this.steps.replace(stepsCopy);
    }

    @action
    _replaceStep(newStep, targetStateId) {
        // grab all steps up to but not including the target
        // then add the new step and commit
        const targetIndex = this._findStepIndex(targetStateId);
        const stepsCopy = this.steps.slice(0, targetIndex);
        stepsCopy.push(newStep);
        this.steps.replace(stepsCopy);
    }

    @action
    changeState(stateId) {
        if (!stateId) {
            this._throwError('stateId is required');
        }

        const currentStep = this.steps[this.steps.length - 1];

        if (this.stateId === stateId && currentStep.stateId === stateId) {
            console.warn(`stateId already set to ${stateId}`);
        }

        currentStep.stateId = stateId;
        this.stateId = stateId;

        LOGGER.debug(`changed stateId to ${stateId}`);
    }

    _findStep(stateId) {
        const matches = this.steps.filter(step => step.stateId === stateId);

        if (matches.length === 1) {
            return matches[0];
        }

        return null;
    }

    _findStepIndex(stateId) {
        const step = this._findStep(stateId);
        return this.steps.indexOf(step);
    }

    /**
     * Return all steps up to and including the step with specified stateId.
     * @param stateId
     * @returns {Array}
     * @private
     */
    _sliceSteps(stateId) {
        const targetIndex = this._findStepIndex(stateId);
        return this.steps.slice(0, targetIndex + 1);
    }

    isStateAdded(stateId) {
        return this._findStep(stateId) !== null;
    }

    @action
    setPlaceholders(placeholders) {
        let array = [];

        if (typeof placeholders === 'string') {
            array.push(placeholders);
        } else if (placeholders) {
            array = placeholders;
        }

        this.placeholders.replace(array);
    }

    completeFlow(payload) {
        if (this.listener && this.listener.onComplete) {
            this.listener.onComplete(payload);
        }
    }

    _setupStates() {
        this.states = this.getStates();
    }

    @action
    _setupInitialStep() {
        const initial = this.getInitialStep();
        this.renderStep(initial);
    }

    @action
    _reset() {
        // these collections should be observable but we don't want elements themselves to be observable
        // (some of them are React elements and making them observable leads to strange runtime errors)
        this.stateId = null;
        this.states = [];
        this.steps = observable(asFlat([]));
        this.placeholders = observable(asFlat([]));

        this.listener = null;
    }

    _throwError(errorString) {
        console.error(errorString);
        throw new Error(errorString);
    }

}

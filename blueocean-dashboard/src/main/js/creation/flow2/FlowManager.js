import { action, asFlat, computed, observable } from 'mobx';

/**
 * Base class for managing the flow of multiple steps.
 * Must provide an initial step, and has methods for pushing or replacing steps on stack.
 */
export default class FlowManager {

    @computed
    get activeIndex() {
        return this.activeSteps.length > 0 ?
            this.activeSteps.length - 1 : 0;
    }

    /**
     * Sets up the initial state of the flow. Do not override.
     * @param listener
     */
    initialize(listener) {
        this._reset();

        this.listener = listener;

        this._setInitialSteps();
        this.onInitialized();
    }

    /**
     * Return a React component (enclosed by FlowStep) that starts the flow
     */
    getInitialStep() {
        throw new Error('need initial step');
    }

    /**
     * Callback invoked after initial step is set up
     */
    onInitialized() {}

    @action
    pushStep(step) {
        this.activeSteps.push(step);
        this._stepsChanged();
    }

    @action popStep() {
        const step = this.activeSteps.pop();
        this._stepsChanged();
        return step;
    }

    @action
    replaceCurrentStep(step) {
        this.activeSteps.splice(-1, 1, step);
        this._stepsChanged();
    }

    @action
    setPendingSteps(steps) {
        this.pendingSteps.replace(steps || []);
    }

    completeFlow(payload) {
        if (this.listener && this.listener.onComplete) {
            this.listener.onComplete(payload);
        }
    }

    @action
    _setInitialSteps() {
        const initial = this.getInitialStep();
        this.activeSteps.replace([initial]);
    }

    _reset() {
        // these collections should be observable but we don't want elements themselves to be observable
        // (some of them are React elements and making them observable leads to strange runtime errors)
        this.activeSteps = observable(asFlat([]));
        this.pendingSteps = observable(asFlat([]));
        this.listener = null;
    }

    _stepsChanged() {
        if (this.listener && this.listener.onStepsChanged) {
            this.listener.onStepsChanged();
        }
    }

}

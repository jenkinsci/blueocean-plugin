/**
 * Created by cmeyers on 11/30/16.
 */
export default class FlowManager {

    constructor() {
        this.listener = null;
        this.activeSteps = [];
        this.pendingSteps = [];
    }

    getInitialStep() {
        throw new Error('need initial step');
    }

    onInitialize() {
        return null;
    }

    initialize(listener) {
        console.log('FlowManager.initialize');

        if (listener && listener.stepsChanged) {
            this.listener = listener;
        }

        this.onInitialize();

        const initial = this.getInitialStep();
        this.pushStep(initial);
    }

    pushStep(step) {
        this.activeSteps.push(step);
        this.listener.stepsChanged();
    }

    setPendingSteps(steps) {

    }

}

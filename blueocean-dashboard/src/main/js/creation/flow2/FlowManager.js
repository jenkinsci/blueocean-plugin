/**
 * Created by cmeyers on 11/30/16.
 */
import { action, asFlat, observable } from 'mobx';

export default class FlowManager {

    // TODO: observable activeSteps blows up React; figure out why
    /*
    @observable
    activeSteps = [];

    @observable
    pendingSteps = [];
    */

    constructor() {
        this.activeSteps = observable(asFlat([]));
        this.pendingSteps = observable(asFlat([]));
        this.listener = null;
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

    @action
    pushStep(step) {
        this.activeSteps.push(step);
        this.listener.stepsChanged();
    }

    @action
    replaceCurrentStep(step) {
        this.activeSteps.splice(-1, 1, step);
        this.listener.stepsChanged();
    }

    @action
    setPendingSteps(steps) {
        this.pendingSteps.replace(steps);
    }

}

/**
 * Created by cmeyers on 10/21/16.
 */

/**
 * Models a SCM Provider (e.g. Git, Github, Github Enterprise) used in 'Create Pipeline' flow.
 * Extend this class and implement methods to provide an implementation.
 */
export default class ScmProvider {

    /**
     * Return a React element (button) that when selected will start the "creation flow"
     * Call props.onSelect to initiate the creation flow.
     */
    getDefaultOption() {
        throw new Error('must implement getDefaultOption');
    }

    /**
     * Return a React element that composes MultiStepFlow with one or more child FlowStep elements.
     * Call props.onCompleteFlow to finish the flow.
     */
    getCreationFlow() {
        throw new Error('must implement getCreationFlow');
    }

    getRentrantOption() {
        return null;
    }

    getRentrantFlow() {
        return null;
    }

}

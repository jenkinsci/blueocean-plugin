/**
 * Created by cmeyers on 10/21/16.
 */

/**
 * Models a SCM Provider (e.g. Git, Github, GitHub Enterprise) used in 'Create Pipeline' flow.
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
     * Return a FlowManager subclass which will return the initial step to begin.
     */
    getFlowManager() {
        throw new Error('must implement getFlowManager');
    }

    /**
     * Called when the related creation flow is about to exit.
     * Similar to React's componentWillUnmount, perform cleanup of event listeners, timers, etc.
     */
    destroyFlowManager() {
        throw new Error('must implement destroyFlowManager');
    }
}

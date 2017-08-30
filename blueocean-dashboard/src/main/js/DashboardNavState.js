import { action, observable } from 'mobx';


/**
 * Used to track whether any route from blueocean-dashboard is currently active.
 * Managed via mount/unmount in top-level Dashboard component.
 */
class DashboardNavState {

    @observable
    isActive = false;

    @action
    setActive() {
        this.isActive = true;
    }

    @action
    setInactive() {
        this.isActive = false;
    }

}

export default new DashboardNavState();

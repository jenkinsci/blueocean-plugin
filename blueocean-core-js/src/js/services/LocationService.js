import { observable, action } from 'mobx';

/**
 * Stores the previous and current location pathnames.
 */
export class LocationService {
    @observable current;
    @observable previous;
    navCount = 0;

    @action setCurrent(newLocation) {
        if (newLocation.action !== 'REPLACE') {
            this.previous = this.current;
        }

        this.current = newLocation.pathname;
    }

    back() {
        // have to subtract 2 because we add one when the route changes
        this.navCount -= 2;
    }
}

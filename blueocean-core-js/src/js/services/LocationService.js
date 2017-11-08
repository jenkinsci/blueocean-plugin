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
}

import { observable, action } from 'mobx';

/**
 * Stores the previous and current pathnames.
 */
export default class LocationService {
    @observable current;
    @observable previous;

    @action setCurrent(newLocation) {
        if (newLocation.action !== 'REPLACE') {
            this.current = this.previous;
        }

        this.current = newLocation.pathname;
    }
}

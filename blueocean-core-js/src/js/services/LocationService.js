import { observable, action } from 'mobx';

export default class LocationService {
    @observable current;
    @observable previous;

    @action setCurrent(current) {
        this.previous = this.current;
        this.current = current;
    }
}
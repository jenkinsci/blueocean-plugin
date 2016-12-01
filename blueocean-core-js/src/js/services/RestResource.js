import { observable, action } from 'mobx';
import { Fetch } from '../fetch';
export class RestResource {
    @observable error;
    @observable pending = false;
    @observable data;

    constructor(url) {
        this.url = url;
    }

    static fetch(url) {
        return new RestResource(url)._fetch();
    }

    @action
    _fetch() {
        this.pending = true;
        this._promise = Fetch.fetchJSON(this.url).then(action('set data', data => {
            this.data = data;
            this.pending = false;
        })).catch(action('set error', error => {
            this.error = error;
            this.pending = false;
        }));

        return this;
    }

    isLoaded() {
        return !this.pending && this.data;
    }
}

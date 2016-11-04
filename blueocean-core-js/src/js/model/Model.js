import { observable } from 'mobx';

export default class Model {
    @observable
    _data = {}

    constructor(data, mapper) {
        if (!mapper) {
            throw new Error('mapper is a requirement');
        }
        this.data = data;
    }

    set data(data) {
        this._data = this.mapper(data);
    }
}
// @flow
import { observable } from 'mobx';

export class Model<T> {
    @observable _data: T;

    constructor(data: T) {
        this._data = data;
    }

    setData(data: T) {
        this._data = data;
    }
}
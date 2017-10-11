import { observable, action, asMap } from 'mobx';
export class DataBunker {
    @observable _data = asMap();

    constructor(keyFn, mapperFn) {
        this._keyFn = keyFn;
        this._mapperFn = mapperFn;
        if (!mapperFn) {
            // identity function.
            this._mapperFn = x => x;
        }
    }

    @action
    setItem(item) {
        const keyItem = this._keyFn(item);
        const mappedItem = this._mapperFn(item);
        this._data.set(keyItem, mappedItem);
        return mappedItem;
    }

    setItems(items) {
        return items.map(item => this.setItem(item));
    }

    getItem(key) {
        return this._data.get(key);
    }

    @action
    removeItem(key) {
        console.log('before', this._data);
        console.log('successful', this._data.delete(key));
        console.log('after', this._data);
    }
}

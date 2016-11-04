import { observable, action, asMap } from 'mobx';
export class DataBunker {
    @observable _data = asMap();

    constructor(keyFn, mapperFn) {
        this._keyFn = keyFn;
        this._mapperFn = mapperFn;
    }

    @action
    setItem(item) {
        this._data.set(this._keyFn(item), this._mapperFn(item));
    }

    setItems(items) {
        for (const item of items) {
            this.setItem(item);
        }
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

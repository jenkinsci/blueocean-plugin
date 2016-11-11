import { DataBunker } from '../model/DataBunker';
import { observable, action, asMap } from 'mobx';

export class BunkerService {
    @observable _data = asMap();

    constructor(pagerService) {
        this.pagerService = pagerService;
    }

    bunkerKey(data) {
        return data._links.self.href;
    }

    bunkerMapper(data) {
        return data;
    }

    refreshPagers() {
        this.pagerService.refresh(this);
    }

    @action
    setItem(item) {
        const keyItem = this.bunkerKey(item);
        const mappedItem = this.bunkerMapper(item);    
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
        this._data.delete(key);      
    }

    hasItem(key) {
        return this._data.has(key);
    }
}

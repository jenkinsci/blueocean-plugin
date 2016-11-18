import { DataBunker } from '../model/DataBunker';
import { observable, computed, action, asMap } from 'mobx';

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
        const mappedItem = observable(this.bunkerMapper(item));    
        const keyItem = this.bunkerKey(mappedItem);
        this._data.set(keyItem, mappedItem);
        return this.getItem(keyItem);
    }


    setItems(items) {
        return items.map(item => this.setItem(item));
    }

    getItem(key) {
        return computed(() => this._data.get(key)).get();
    }

    @action
    removeItem(key) {
        this._data.delete(key);     
    }

    hasItem(key) {
        return this._data.has(key);
    }
}

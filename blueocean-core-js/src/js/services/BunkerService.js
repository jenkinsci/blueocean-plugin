import { observable, computed, action, asMap } from 'mobx';

/**
 * Abstract class used by services that need to store data in a key/value store.
 *
 * It is designed to store json objects from a rest api.
 *
 * @export
 * @class BunkerService
 */
export class BunkerService {
    /**
     * Private mobx map for storing data.
     */
    @observable _data = asMap();

    /**
     * Creates an instance of BunkerService.
     *
     * @param {PagerService} pagerService
     */
    constructor(pagerService) {
        this.pagerService = pagerService;
    }

    /**
     * Extracts the key to store the data under out of the object
     *
     * Default impl uses the self href link in BlueOcean objects.
     *
     * @param {object} data Data to be stored once it has been passed through
     *                      [@link bunkerMapper]
     * @returns {any} The key for the store.
     */
    bunkerKey(data) {
        return data._links.self.href;
    }

    /**
     * Maps the data from the source into what needs to be stored.
     *
     * Default impl is identity.
     *
     * @param {object} data Raw data from external source.
     * @returns {object} Modified data object.
     */
    bunkerMapper(data) {
        return data;
    }

    /**
     * Helper function that will make all pagers using this bunker refetch the data
     * they are displaying. Useful if sorting changes (for example a new item is added).
     */
    refreshPagers() {
        this.pagerService.refresh(this);
    }

    /**
     * Sets an item in the store.
     *
     * It uses the [@link bunkerKey] and [@link bunkerMapper] to generate the key/value
     * to be stored.
     *
     * @param {Object} item Raw data from external source.
     * @returns {Object} item mapped by [@link bunkerMapper]. It is also a mobx computed value.
     */
    @action
    setItem(item) {
        const mappedItem = observable(this.bunkerMapper(item));
        const keyItem = this.bunkerKey(mappedItem);
        this._data.set(keyItem, mappedItem);
        return this.getItem(keyItem);
    }

    /**
     * Sets an array on item in the store. Calls [@link setItem] for even item in array.
     *
     * @param {Object[]} items Array of items to set.
     * @returns {Object[]} Array of mobx computed values from store.
     */
    setItems(items) {
        return items.map(item => this.setItem(item));
    }

    /**
     * Gets item from store.
     *
     * @param {any} key Key of item in store.
     * @returns {Object} Mobx computed value of value in store.
     */
    getItem(key) {
        return computed(() => this._data.get(key)).get();
    }

    /**
     * Removes item from store.
     *
     * @param {any} key Key of item in store.
     */
    @action
    removeItem(key) {
        this._data.delete(key);
    }

    /**
     * Tests to see if item exists in store.
     *
     * @param {any} key Key of item in store.
     * @returns {boolean} true if item exists in store.
     */
    hasItem(key) {
        return this._data.has(key);
    }
}

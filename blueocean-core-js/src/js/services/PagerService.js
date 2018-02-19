/**
 * This service manages the various instances of pagers that currently exist.
 *
 * TODO: Currently a new pager is created for any new list of items to be paged. Cleanup may be
 * required to stop memory leaks. However Pagers don't store more data so this may not be an issue.'
 *
 * @export
 * @class PagerService
 */
export class PagerService {
    /**
     * MobX map to hold [@link Pager]'s'
     */
    _pagerMap = new Map();

    /**
     * Registers a pager with the PagerService.
     *
     * Namespacing strings is prefered to stop collisions. E.g. Activity/$org-$pipeline.
     *
     * @param {any} key - Key to register the pager under.
     * @param {Pager} pager - pager to register.
     */
    registerPager(key, pager) {
        if (this._pagerMap.has(key)) {
            throw new Error(`Pager '${key}' already exits in PagerService`);
        }
        this._pagerMap.set(key, pager);
    }

    /**
     * Removes pager from the cache.
     *
     * @param {any} key
     */
    removePager(key) {
        if (this._pagerMap.has(key)) {
            this._pagerMap.delete(key);
        }
    }

    /**
     * Lazily creates a pager. Do this because pager fetches the first page when it is created.
     *
     * @callback lazyPager
     * @returns {Pager}
     */
    /**
     * Gets a pager from the cache.
     *
     * @param {Object} options
     * @param {any} options.key - Key to store pager under.
     * @param {lazyPager} options.lazyPager - function to lazily crete the pager.
     * @returns {Pager}
     */
    getPager({ key, lazyPager }) {
        if (this._pagerMap.has(key)) {
            return this._pagerMap.get(key);
        }
        if (lazyPager) {
            const pager = lazyPager();
            this.registerPager(key, pager);
            return pager;
        }

        return null;
    }

    /**
     * Refetches the list of items the pagers that use a specific [@link BunkerService] to display.
     * This is done in the case of reordering.
     *
     * TODO: Make this more targetted.
     *
     * @param {BunkerService} bunkerService A service that extends [@link BunkerService]
     */
    refresh(bunkerService) {
        this._pagerMap.forEach(pager => {
            if (bunkerService === pager.bunker) {
                pager.refresh();
            }
        });
    }

    /**
     * Gets all pagers for a [@link BunkerService]
     *
     * @param {BunkerService} bunker
     * @returns {Pager[]}
     */
    getPagers(bunker) {
        const ret = [];
        this._pagerMap.forEach(pager => {
            if (bunker === pager.bunker) {
                ret.push(bunker);
            }
        });

        return ret;
    }
}


export default class PagerService {
    _pagerMap = new Map();

    registerPager(key, pager) {
        if (this._pagerMap.has(key)) {
            throw new Error(`Pager '${key}' already exits in PagerService`);
        }
        this._pagerMap.set(key, pager);
    }

    removePager(key) {
        if (this._pagerMap.has(key)) {
            this._pagerMap.delete(key);
        }
    }

    getPager({ key, lazyPager }) {
        if (this._pagerMap.has(key)) {
            return this._pagerMap.get(key);
        }
        const pager = lazyPager();
        this.registerPager(key, pager);
        return pager;
    }

    invalidatePagerHrefs(bunker) {
        this._pagerMap.forEach(pager => {
            if (bunker === pager.bunker) {
                pager.refetchHrefs();
            }
        });
    }
}


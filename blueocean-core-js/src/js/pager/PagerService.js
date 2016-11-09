export class PagerService {
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
        if(lazyPager) {
            const pager = lazyPager();
            this.registerPager(key, pager);
            return pager;
        }

        return null;
    }

    refresh(bunker) {
        console.log('invalidate bunker', bunker);
        this._pagerMap.forEach(pager => {
            if (bunker === pager.bunker) {
                console.log('refresh bunker');
                pager.refresh();
            }
        });
    }

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


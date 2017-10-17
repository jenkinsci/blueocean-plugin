/**
 * Basic data caching with simple eviction
 */
export class DataCache {
    _cache = {};
    _ttl = 5 * 60 * 1000; // default live for 5 minutes
    _evictionFrequency = 10 * 1000; // default run every 10 sec

    constructor({ ttl } = {}) {
        if (ttl) {
            this._ttl = ttl;
        }
        this._queueEviction();
    }

    destroy() {
        clearTimeout(this.evictionThread);
        delete this.evictionThread;
    }

    put(key, obj) {
        this._cache[key] = {
            time: Date.now(),
            data: obj,
        };
    }

    get(key) {
        const entry = this._cache[key];
        if (entry) {
            return entry.data;
        }
        return null;
    }

    has(key) {
        return key in this._cache;
    }

    /**
     * Removes an item, optionally accepts a pattern to remove
     * @param {string|pattern} key direct key or pattern
     */
    remove(key) {
        if (key instanceof RegExp) {
            this._removeMatching(key);
        }
        delete this._cache[key];
    }

    _removeMatching(pattern) {
        for (const key of Object.keys(this._cache)) {
            const entry = this._cache[key];
            if (entry && pattern.test(key)) {
                delete this._cache[key];
            }
        }
    }

    _queueEviction() {
        this.evictionThread = setTimeout(() => {
            try {
                const now = Date.now();
                for (const key of Object.keys(this._cache)) {
                    const entry = this._cache[key];
                    if (entry) {
                        if (now - entry.time > this._ttl) {
                            delete this._cache[entry.key];
                        }
                    }
                }
            } finally {
                this._queueEviction();
            }
        }, this._evictionFrequency);
    }
}

export default new DataCache();

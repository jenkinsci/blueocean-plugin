/**
 * A state-tree walker, can be used to find objects in the redux state
 * tree and replace them, or any object graph. Has special handling for
 * Immutable.js data structures.
 */

import Immutable from 'immutable';

const debugLog = require('debug')('find-and-update:debug');

// Main body of findAndUpdate
function _findAndUpdate(obj, replacer, visited, path) {
    if (!obj || typeof obj === 'boolean' || typeof obj === 'number' || typeof obj === 'string') {
        return undefined;
    }
    debugLog('checking: ', path, obj);
    const visit = visited.stop(obj);
    if (visit) {
        debugLog('was already visited object at path: ', path, visit);
        if (visit.replacement) {
            debugLog('found already visited replacement at path: ', path, visit);
        }
        return visit.replacement; // usually undefined
    }
    const val = replacer(obj);
    if (val) {
        debugLog('found replacement at path: ', path, val);
        visited.setReplaced(obj, val);
        return val;
    }
    if (obj instanceof Array || obj instanceof Immutable.Iterable.Indexed) {
        let updated = false;
        let idx = 0;
        const next = obj.map(curr => {
            // retains $pager info
            path.push(idx);
            const other = _findAndUpdate(curr, replacer, visited, path);
            path.pop();
            idx++;
            if (other) {
                updated = other;
                return other;
            }
            return curr;
        });
        if (updated) {
            debugLog('updated array/Immutable.Indexed at path: ', path, 'new: ', obj, next);
            visited.setReplaced(obj, next || obj);
            return next || obj; // Iterable.map sometimes may mutate?
        }
    } else if (obj instanceof Immutable.Record) {
        let updated = false;
        let o = obj;
        for (const k of o.keySeq().toArray()) {
            const v = o[k];
            path.push(k);
            const next = _findAndUpdate(v, replacer, visited, path);
            path.pop();
            if (next) {
                updated = true;
                o = o.set(k, next);
            }
        }
        if (updated) {
            debugLog('Updated immutable record: ', path, obj);
            visited.setReplaced(obj, o);
            return o;
        }
    } else {
        let updated = false;
        let o = obj;
        for (const k in obj) {
            if (obj.hasOwnProperty(k)) {
                path.push(k);
                const next = _findAndUpdate(o[k], replacer, visited, path);
                path.pop();
                if (next) {
                    debugLog('found replacement for obj: ', o, k, next);
                    updated = true;
                    o = Object.assign({}, o);
                    o[k] = next;
                }
            }
        }
        if (updated) {
            debugLog('Updated object at path: ', path, o);
            visited.setReplaced(obj, o);
            return o;
        }
    }
    return null;
}

/**
 * Locates instances in the provided object graph and replaces them, just provide a 'replacer' method,
 * which returns undefined/false for objects which should not be replaced.
 */
export default function findAndUpdate(obj, replacer) {
    try {
        debugLog('findAndUpdate called with: ', obj, 'from:', new Error(), 'with replacer:', replacer);
        const noReplacement = {};
        const out = _findAndUpdate(
            obj,
            replacer,
            {
                visited: [],
                replaced: [],
                stop(o) {
                    const idx = this.visited.indexOf(o);
                    if (idx >= 0) {
                        const replacement = this.replaced[idx];
                        if (replacement) {
                            return replacement;
                        }
                        return noReplacement;
                    }
                    this.visited.push(o);
                    this.replaced.push(undefined);
                    return false;
                },
                setReplaced(o, replacement) {
                    const idx = this.visited.indexOf(o);
                    if (idx < 0) {
                        throw new Error('Unable to find visited value.');
                    }
                    this.replaced[idx] = { replacement };
                },
            },
            []
        );
        debugLog('findAndUpdateDone: ', out || obj);
        return out || obj;
    } catch (e) {
        throw e;
    }
}

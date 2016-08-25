/**
 * A state-tree walker, can be used to find objects in the redux state
 * tree and replace them, or any object graph. Has special handling for
 * Immutable.js data structures.
 */

import Immutable from 'immutable';

const debugLog = require('debug')('find-and-update:debug');

// Main body of findAndUpdate
function _findAndUpdate(obj, replacer, visited) {
    if (!obj || typeof obj === 'boolean' || typeof obj === 'number' || typeof obj === 'string') {
        return undefined;
    }
    const visit = visited.stop(obj);
    if (visit) {
        return visit.replacement; // usually undefined
    }
    const val = replacer(obj);
    if (val) {
        visited.setReplaced(obj, val);
        return val;
    }
    if (obj instanceof Array || obj instanceof Immutable.Iterable.Indexed) {
        let updated = false;
        const next = obj.map(curr => { // retains $pager info
            const other = _findAndUpdate(curr, replacer, visited);
            if (other) {
                debugLog('found replacement in map: ', other);
                updated = other;
                return other;
            }
            return curr;
        });
        if (updated) {
            debugLog('updated array/Immutable.Indexed: ', obj, next);
            return next || obj; // Iterable.map sometimes may mutate?
        }
    } else if (obj instanceof Immutable.Record) {
        let updated = false;
        let o = obj;
        for (const k of o.keySeq().toArray()) {
            const v = o[k];
            const next = _findAndUpdate(v, replacer, visited);
            if (next) {
                debugLog('found replacement for Immutable.Record: ', o, k, next);
                updated = true;
                o = o.set(k, next);
            }
        }
        if (updated) {
            debugLog('returning from IR: ', o);
            return o;
        }
    } else {
        let updated = false;
        let o = obj;
        for (const k in obj) {
            if (obj.hasOwnProperty(k)) {
                const next = _findAndUpdate(o[k], replacer, visited);
                if (next) {
                    debugLog('found replacement for obj: ', o, k, next);
                    updated = true;
                    o = Object.assign({}, o);
                    o[k] = next;
                }
            }
        }
        if (updated) {
            debugLog('returning: ', o);
            return o;
        }
    }
    debugLog('no replacement found for: ', obj);
    return null;
}

/**
 * Locates instances in the provided object graph and replaces them, just provide a 'replacer' method,
 * which returns undefined/false for objects which should not be replaced.
 */
export default function findAndUpdate(obj, replacer) {
    try {
        debugLog('findAndUpdate called with: ', obj, 'from:', new Error(), 'with replacer:', replacer);
        const noReplacement = { };
        const out = _findAndUpdate(obj, replacer, {
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
        });
        debugLog('findAndUpdateDone: ', out || obj);
        return out || obj;
    } catch (e) {
        throw e;
    }
}

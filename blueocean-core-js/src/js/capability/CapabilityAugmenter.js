/**
 * Created by cmeyers on 9/8/16.
 */

const addClass = (clazz, classMap) => {
    const className = clazz._class;

    if (!classMap[className]) {
        classMap[className] = [];
    }

    classMap[className].push(clazz);
};

const canWalk = (item) => item && (typeof item === 'object' || Array.isArray(item));

// TODO: can we really filter out 'actions' in all cases? should it use a leading underscore?
const IGNORED_PROPS = ['_links', 'actions'];

/**
 *
 */
export class CapabilityAugmenter {

    constructor(capabilityStore) {
        this._capabilityStore = capabilityStore;
    }

    augmentCapabilities(data) {
        const classMap = this._findClassesInTree(data);
        return classMap;
    }

    _findClassesInTree(data) {
        const classMap = {};
        const nodesToWalk = [data];
        let node = nodesToWalk.shift();

        while (node) {
            if (typeof node === 'object' && node._class) {
                addClass(node, classMap);
            }

            const nodeKeys = Object.keys(node);

            for (const key of nodeKeys) {
                const value = node[key];

                if (canWalk(value) && IGNORED_PROPS.indexOf(key) === -1) {
                    nodesToWalk.push(value);
                }
            }

            node = nodesToWalk.shift();
        }

        return classMap;
    }

}

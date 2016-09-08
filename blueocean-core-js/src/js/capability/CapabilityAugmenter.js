/**
 * Created by cmeyers on 9/8/16.
 */

const addAugment = (target, allTargets) => {
    const className = target._class;

    if (!allTargets[className]) {
        allTargets[className] = [];
    }

    allTargets[className].push(target);
};

const canWalk = (item) => item && (typeof item === 'object' || Array.isArray(item));

const IGNORED_PROPS = ['_links', '_actions'];

/**
 *
 */
export class CapabilityAugmenter {

    constructor(capabilityStore) {
        this._capabilityStore = capabilityStore;
    }

    augmentCapabilities(data) {
        const augmentTargets = this._walkObjectTree(data);
        return augmentTargets;
    }

    _walkObjectTree(data) {
        const augmentTargets = {};
        const nodesToWalk = [data];
        let node = nodesToWalk.shift();

        while (node) {
            if (typeof node === 'object' && node._class) {
                addAugment(node, augmentTargets);
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

        return augmentTargets;
    }

}

/**
 * Created by cmeyers on 9/15/16.
 */
import {
    capabilityAugmenter as augmenter,
    capabilityStore as store,
} from '@jenkins-cd/blueocean-core-js';

/**
 * Utilities for setting up test data with associated capabilities.
 */
export class CapabilityTestUtils {
    /**
     * Bind the supplied className to the given list of capabilities.
     * The first capability is always the original className.
     * Note that this binding will persist unless unbindAll is called.
     *
     * @param className
     * @param capabilities
     */
    bindCapability(className, ...capabilities) {
        store._localStore[className] = [className, ...capabilities];
    }

    /**
     * Populate the "_capabilities" property of all elements in "data" which have a "_class" name.
     * Will respect any previous calls to bindCapability. For classNames not previously bound,
     * the object will receive a single capability equaling the value of its '_class' property.
     *
     * @param data
     * @returns {Promise}
     */
    augment(data) {
        const classMap = augmenter._findClassesInTree(data);

        for (const className of Object.keys(classMap)) {
            if (!store._localStore[className]) {
                store._localStore[className] = [className];
            }
        }

        return augmenter.augmentCapabilities(data);
    }

    /**
     * Unbinds all capabilities previously registered.
     */
    unbindAll() {
        store._localStore = {};
    }
}

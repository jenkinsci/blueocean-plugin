/**
 * Created by cmeyers on 8/18/16.
 */
import { classMetadataStore } from '@jenkins-cd/js-extensions';

/**
 * Setup a list of capabilities for the provided className.
 * Useful unit-testing a component that is using ClassMetadataStore internally since
 * calls to it from a test will typically fail.
 *
 * @param className
 * @param capabilities one or more capabilities to be added
 */
export const bindCapability = (className, ...capabilities) => {
    if (!classMetadataStore.classMetadata) {
        classMetadataStore.init(() => null);
    }

    let metadata = classMetadataStore.classMetadata[className];

    if (!metadata) {
        metadata = classMetadataStore.classMetadata[className] = {};
        metadata.classes = [
            className
        ];
    }

    metadata.classes = metadata.classes.concat(capabilities);

};

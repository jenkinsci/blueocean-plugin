/**
 * Created by cmeyers on 9/9/16.
 */

/**
 * Determines whether the supplied object has at least one of the supplied capabilities.
 *
 * As capabilities are typically name-spaced, this method will match on long or short names, e.g.
 * given: _capabilities = ['a.b.Capability1']
 * passing either 'a.b.Capability1' or 'Capability1' will match
 *
 * @param {object} subject
 * @param {...string} capabilityNames
 * @returns {boolean}
 */
export const capable = (subject, ...capabilityNames) => {
    if (subject && subject._capabilities) {
        // in case an array was passed in, flatten it out
        const flattenedCapabilities = [].concat(...capabilityNames);

        // find the intersection of subject's caps with the passed-in caps
        const longNameMatches = flattenedCapabilities.filter(longName => subject._capabilities.indexOf(longName) !== -1);
        if (longNameMatches.length > 0) {
            return true;
        }

        // build short form of subject's caps, then find intersection
        const shortNames = subject._capabilities.map(longName =>
            longName
                .split('.')
                .slice(-1)
                .join('')
        );

        const shortNameMatches = flattenedCapabilities.filter(longName => shortNames.indexOf(longName) !== -1);
        if (shortNameMatches.length > 0) {
            return true;
        }
    }

    return false;
};

/**
 * Provides some convenience methods for host object of _class/_capabilities
 */
export class Capable {
    /**
     * Determines whether the host object has the supplied capability.
     * As capabilities are typically name-spaced, this method will match on long or short names, e.g.
     * if _capabilities = ['a.b.Capability1']
     * passing either 'a.b.Capability1' or 'Capability1' will match
     *
     * @param {string} capabilityName
     * @returns {boolean}
     */
    can(capabilityName) {
        return capable(this, capabilityName);
    }
}

export default new Capable();

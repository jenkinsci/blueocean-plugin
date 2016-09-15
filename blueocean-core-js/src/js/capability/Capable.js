/**
 * Created by cmeyers on 9/9/16.
 */

/**
 * Determines whether the supplied object has the supplied capability.
 * As capabilities are typically name-spaced, this method will match on long or short names, e.g.
 * if _capabilities = ['a.b.Capability1']
 * passing either 'a.b.Capability1' or 'Capability1' will match
 *
 * @param {object} subject
 * @param {string} capabilityName
 * @returns {boolean}
 */
export const capable = (subject, capabilityName) => {
    if (subject._capabilities) {
        if (subject._capabilities.indexOf(capabilityName) !== -1) {
            return true;
        }

        for (const capability of subject._capabilities) {
            const shortName = capability.split('.').slice(-1).join('');
            if (shortName === capabilityName) {
                return true;
            }
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

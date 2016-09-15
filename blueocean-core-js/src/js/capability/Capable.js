/**
 * Created by cmeyers on 9/9/16.
 */

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
     * @param capabilityName
     * @returns {boolean}
     */
    can(capabilityName) {
        if (this._capabilities) {
            if (this._capabilities.indexOf(capabilityName) !== -1) {
                return true;
            }

            for (const capability of this._capabilities) {
                const shortName = capability.split('.').slice(-1).join('');
                if (shortName === capabilityName) {
                    return true;
                }
            }
        }

        return false;
    }
}

export default new Capable();

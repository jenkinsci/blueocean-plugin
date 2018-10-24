/**
 * Created by cmeyers on 9/8/16.
 */
import { Fetch } from '../fetch';
import { UrlConfig } from '../urlconfig';
import { Utils } from '../utils';

export class CapabilityApi {
    /**
     * Fetch the capabilities for one or more class names.
     *
     * @param {Array} classNames
     * @returns {Promise} with fulfilled {object} keyed by className, with an array of string capability names.
     * @private
     */
    fetchCapabilities(classNames) {
        const noDuplicates = classNames.filter((item, index, self) => self.indexOf(item) === index);
        const path = UrlConfig.getJenkinsRootURL();
        const classesUrl = Utils.cleanSlashes(`${path}/blue/rest/classes/`);

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ q: noDuplicates }),
        };

        return Fetch.fetchJSON(classesUrl, { disableCapabilities: true, fetchOptions });
    }
}

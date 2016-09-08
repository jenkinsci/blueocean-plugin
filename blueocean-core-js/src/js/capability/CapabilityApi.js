/**
 * Created by cmeyers on 9/8/16.
 */
import { Fetch } from '../fetch';
import config from '../urlconfig';
import utils from '../utils';

export class CapabilityApi {

    /**
     * Fetch the capabilities for one or more class names.
     *
     * @param classNames
     * @returns {Promise} with fulfilled {object} keyed by className, with an array of string capability names.
     * @private
     */
    fetchCapabilities(...classNames) {
        const path = config.getJenkinsRootURL();
        const classList = classNames.join(',');
        const classesUrl = utils.cleanSlashes(`${path}/blue/rest/classes/?q=${classList}`);

        return Fetch.fetchJSON(classesUrl);
    }

}

/**
 * Created by cmeyers on 8/31/16.
 */
import es6Promise from 'es6-promise'; es6Promise.polyfill();

import { Fetch } from '../fetch';
import config from '../urlconfig';
import utils from '../utils';

export const CAPABILITIES = {
    SIMPLE_PIPELINE: 'org.jenkinsci.plugins.workflow.job.WorkflowJob',
    MULTIBRANCH_PIPELINE: 'io.jenkins.blueocean.rest.model.BlueMultiBranchPipeline',
    MULTIBRANCH_BRANCH: 'io.jenkins.blueocean.rest.model.BlueBranch',
};

/**
 * Retrieves capability metadata for class names.
 * Uses an internal cache to minimize REST API calls.
 */
export class CapabilityStore {

    constructor() {
        this.store = {};
    }

    /**
     * Fetch the capabilities for one or more class names.
     * Will used cached values if available.
     *
     * @param classNames
     * @returns {Promise} with fulfilled {object} keyed by className, with an array of string capability names.
     */
    resolveCapabilities(...classNames) {
        const result = {};
        const classesToFetch = [];

        // determine which class names are already in the cache and which aren't
        for (const className of classNames) {
            if (this.store[className]) {
                result[className] = this.store[className];
            } else {
                classesToFetch.push(className);
            }
        }

        // if nothing to fetch, just return an immediately fulfilled Promise
        if (classesToFetch.length === 0) {
            return new Promise(resolve => resolve(result));
        }

        // fetch the capabilities and then merge that with the values already in the cache
        return this._fetchCapabilities(classesToFetch)
            .then(fetchedCapabilities => Object.assign(result, fetchedCapabilities));
    }

    /**
     * Fetch the capabilities for one or more class names.
     *
     * @param classNames
     * @returns {Promise} with fulfilled {object} keyed by className, with an array of string capability names.
     * @private
     */
    _fetchCapabilities(...classNames) {
        const path = config.getJenkinsRootURL();
        const classList = classNames.join(',');
        const classesUrl = utils.cleanSlashes(`${path}/blue/rest/classes/?q=${classList}`);

        return Fetch.fetchJSON(classesUrl)
            .then(data => this._storeCapabilities(data.map));
    }

    /**
     * Store the values in the cache and return it.
     *
     * @param map
     * @returns {object} keyed by className, with an array of string capability names.
     * @private
     */
    _storeCapabilities(map) {
        const storedCapabilities = {};

        Object.keys(map).forEach(className => {
            const capabilities = map[className];
            this.store[className] = storedCapabilities[className] = capabilities.classes.slice();
        });

        return storedCapabilities;
    }
}

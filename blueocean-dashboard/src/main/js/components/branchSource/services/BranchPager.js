import { action, computed, observable } from 'mobx';
import { logging } from '@jenkins-cd/blueocean-core-js';
import { GenericApi } from '../../rest/GenericApi';

const logger = logging.logger('io.jenkins.blueocean.dashboard.BranchPager');

/**
 * The pager fetches pages of data from the BlueOcean api. It fetches pages of data, then
 * inserts them into the [@link BunkerService], and stores the href from the data.
 *
 * MobX computes a data field from the href backed by the backend cache. This allows for SSE events
 * to be propagated to the pager.
 *
 * @export
 * @class Pager
 */
export class BranchPager {
    /**
     * pager is fetching data. log and detail
     * @type {boolean}
     */
    @observable pending = false;
    /**
     * Will be set in an error occurs.
     * @type {object|null}
     */
    @observable error = null;

    /**
     * Mobx computed value that creates an object. If either the  bunker changes,
     * or the href change, this is recalculated and will trigger a react reaction.
     *
     * If item does not exist in bunker, then we just ignore it.
     * @readonly
     * @type {object}
     */
    @computed
    get branch() {
        return this.bunker.getItem(this.url);
    }
    /**
     * Creates an instance of Pager and fetches the first page.
     *
     * @param {BunkerService} bunker - Data store
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} run Run that this pager belongs to.
     */
    constructor(bunker, url) {
        this.url = url;
        this.fetchBranchDetails();
    }

    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchBranchDetails() {
        // while fetching we are pending
        this.pending = true;

        return GenericApi.getHref(this.url)
            .then(action('Process pager data', result => {
                debugger
                this.bunker.setItem(result);
                this.pending = false;
            })).catch(err => {
                logger.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }
}

import { observable, action, computed } from 'mobx';
import { logging, capable } from '@jenkins-cd/blueocean-core-js';

import { KaraokeApi } from '../index';
import { FREESTYLE_JOB, PIPELINE_JOB } from '../../../Capabilities';

const logger = logging.logger('io.jenkins.blueocean.dashboard.karaoke.Pager');

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
export class Pager {
    /**
     * List of deisplayed items hrefs.
     */
    @observable href;
    /**
     * pager is fetching data.
     */
    @observable pending = false;
    /**
     * Will be set in an error occurs.
     */
    @observable error = null;
    /**
     * The latest page the pager has fetched.
     */
    @observable currentPage = 0;

    @observable isFreeStyle = false;
    @observable isPipeline = false;
    @observable generalLogUrl;

    /**
     * Mobx computed value that creates an object. If either the  bunker changes,
     * or the href change, this is recalculated and will trigger a react reaction.
     *
     * If item does not exist in bunker, then we just ignore it.
     * @readonly
     * @type {object}
     */
    @computed
    get data() {
        return this.bunker.getItem(this.href);
    }
    /**
     * Creates an instance of Pager and fetches the first page.
     *
     * @param {BunkerService} bunker - Data store
     * @param {object} pipeline Pipeline that this pager belongs to.
     * @param {string} branch the name of the branch we are requesting
     * @param {string} runId Run that this pager belongs to.
     */
    constructor(bunker, pipeline, branch, runId) {
        this.bunker = bunker;
        this.pipeline = pipeline;
        this.branch = branch;
        this.runId = runId;
        // Fetch the first page so that the user does not have to.
        this.fetchPage();
    }

    /**
     * Fetches the detail from the backend and set the data
     *
     * @returns {Promise}
     */
    @action
    fetchPage() {
        // while fetching we are pending
        this.pending = true;
        // get api data and further process it
        return KaraokeApi.getRunWithId(this.pipeline, this.branch, this.runId)
            .then(action('Process pager data', data => {
                // Store item in bunker.
                const saved = this.bunker.setItem(data);
                logger.warn('saved data', saved);
                // Append the new Href to the existing ones.
                // debugger;
                this.href = saved._links.self.href;
                this.generalLogUrl = saved._links.log.href;
                this.isFreeStyle = capable(saved, FREESTYLE_JOB);
                this.isPipeline = capable(saved, PIPELINE_JOB);
                return this.isPipeline;
            }))
            .then(action('Process post data', isPipeline => {
                if (isPipeline) {
                    logger.debug('Here we need to do additional fetching');
                }
                this.pending = false;
            })).catch(err => {
                console.error('Error fetching page', err);
                action('set error', () => { this.error = err; });
            });
    }
}

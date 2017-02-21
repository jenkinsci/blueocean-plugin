import { Pager } from './Pager';
import RestPaths from '../paths/rest';
import { Fetch } from '../fetch';
import { BunkerService } from './BunkerService';
import utils from '../utils';
import mobxUtils from 'mobx-utils';

/*
 * This class provides activity related services.
 *
 * @export
 * @class ActivityService
 * @extends {BunkerService}
 */
export class ActivityService extends BunkerService {
    /**
     * Generates a pager key for [@link PagerService] to store the [@link Pager] under.
     *
     * @param {string} organization Jenkins organization that this pager belongs to.
     * @param {string} pipeline Pipeline that this pager belongs to.
     * @returns {string} key for [@link PagerService]
     */
    pagerKey(organization, pipeline) {
        return `Activities/${organization}-${pipeline}`;
    }

    /**
     * Gets the activity pager
     *
     * @param {string} organization Jenkins organization that this pager belongs to.
     * @param {string} pipeline Pipeline that this pager belongs to.
     * @returns {Pager} Pager for this pipelne.
     */
    activityPager(organization, pipeline, branch) {
        return this.pagerService.getPager({
            key: this.pagerKey(organization, pipeline) + '-' + branch,
            /**
             * Lazily generate the pager incase its needed.
             */
            lazyPager: () => new Pager(RestPaths.activities(organization, pipeline, branch), 25, this),
        });
    }

    /**
     * Maps queued data into a psudeorun
     *
     * @see _mapQueueToPsuedoRun
     *
     * @param {Object} data Raw data from extenal source.
     * @returns A run or psudeorun.
     */
    bunkerMapper(data) {
        return this._mapQueueToPsuedoRun(data);
    }

    /**
     * Gets an activity from the store.
     *
     * @param {string} href Self href for activity.
     * @returns {object} Mobx computed value
     */
    getActivity(href) {
        return this.getItem(href);
    }

    /**
     * Fetches an activity from rest api.
     *
     * Note: This only works for activities that are not in the queue.
     *
     * @param {string} href self href of activity.
     * @param {boolean} useCache Use the cache to lookup data or always fetch a new one.
     * @param {boolean} overrideQueuedState Hack to make SSE work. Not use unless you know what you are doing!!!
     * @returns {Promise} Promise of fetched data.
     */
    fetchActivity(href, { useCache, overrideQueuedState } = {}) {
        if (useCache && this.hasItem(href)) {
            return Promise.resolve(this.getItem(href));
        }


        return Fetch.fetchJSON(href)
            .then(data => {
                // Should really have dedupe on methods like these, but for now
                // just clone data so that we dont modify other instances.
                const run = utils.clone(data);

                // Ugly hack to make SSE work.
                if (overrideQueuedState) {
                    run.state = 'RUNNING';
                    run.result = 'UNKNOWN';
                }
                return this.setItem(run);
            })
            .catch(err => {
                console.log('There has been an error while trying to get the data.', err); // FIXME: Ivan what is the way to return an "error" opbject so underlying component are aware of the problem and can react
            });
    }

    /**
     * Fetches artifacts for a given run.
     *
     * @param {string} runHref The href of the run to fetcfh artifacts for.
     * @returns {Object} Object containing zipFile link and list of artifacts.
     */
    fetchArtifacts(runHref) {
        return mobxUtils.fromPromise(Fetch.fetchJSON(`${runHref}artifacts/?start=0&limit=101`));
    }


    /**
     * This function maps a queue item into a run instancce.
     *
     * We do this because the api returns us queued items as well
     * as runs and its easier to deal with them if they are modeled
     * as the same thing. If the raw data is needed if can be fetched
     * from _item.
     *
     * @param {object} run Raw data from api.
     * @returns psudeorun
     */
    _mapQueueToPsuedoRun(run) {
        if (run._class === 'io.jenkins.blueocean.service.embedded.rest.QueueItemImpl') {
            return {
                id: String(run.expectedBuildNumber),
                state: 'QUEUED',
                pipeline: run.pipeline,
                type: 'QueuedItem',
                result: 'UNKNOWN',
                job_run_queueId: run.id,
                enQueueTime: run.queuedTime,
                organization: run.organization,
                changeSet: [],
                _links: {
                    self: {
                        href: `${run._links.parent.href}runs/${run.expectedBuildNumber}/`,
                    },
                    parent: {
                        href: run._links.parent.href,
                    },
                },
                _item: run,
            };
        }
        return run;
    }


    /**
     * Calculate an expected build number for a queued item.
     *
     * TODO: Enhance SSE so that this is done server side.
     *
     * @param {any} event SSE event.
     * @returns {number} Expected build number
     */
    getExpectedBuildNumber(event) {
        const runs = this._data.values();
        const eventJobUrl = event.blueocean_job_rest_url;
        let nextId = 0;
        for (let i = 0; i < runs.length; i++) {
            const run = runs[i];
            if (eventJobUrl !== run._links.parent.href) {
                continue;
            }
            if (run.job_run_queueId === event.job_run_queueId) {
                // We already have a "dummy" record for this queued job
                // run. No need to create another i.e. ignore this event.
                return run.id;
            }
            if (parseInt(run.id, 10) > nextId) { // figure out the next id, expectedBuildNumber
                nextId = parseInt(run.id, 10);
            }
        }

        return nextId + 1;
    }
}

import { Pager } from './Pager';
import RestPaths from '../paths/rest';
import { Fetch } from '../fetch';
import utils from '../utils';
import { BunkerService } from './BunkerService';
import { action } from 'mobx';
/**
 * This class handles pipeline related data. This includes pipelines, branches and pullrequeusts as they are
 * all pipelines in the backend.
 *
 * @export
 * @class PipelineService
 * @extends {BunkerService}
 */
export class PipelineService extends BunkerService {
    /**
     * Creates an instance of PipelineService.
     *
     * @param {PagerService} pagerService
     * @param {ActivityService} activityService
     */
    constructor(pagerService, activityService) {
        super(pagerService);
        this.activityService = activityService;
    }

    /**
     * Gets pager for /blue/organization/:organization/pipelines
     *
     * @param {searchText} search text for filtering pipelines by name.
     * @param {organization} organization organization pager belongs to.
     * @returns {Pager}
     */
    pipelinesPager(organization, searchText) {
        return this.pagerService.getPager({
            key: `searchPipelinesPager/${searchText}/${organization}`,
            lazyPager: () => new Pager(RestPaths.pipelines(organization, searchText), 25, this),
        });
    }

    /**
     * Gets pager for /blue/organization/:organization/pipelines/:pipeline/branches
     *
     * @param {string} organization
     * @param {string} pipeline
     * @returns {Pager}
     */
    branchPager(organization: string, pipeline: string) {
        return this.pagerService.getPager({
            key: `Branches/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.branches(organization, pipeline), 25, this),
        });
    }


    /**
     * Gets pager for /blue/organization/:organization/pipelines/:pipeline/pullRequests
     *
     * @param {string} organization
     * @param {string} pipeline
     * @returns {Pager}
     */
    prPager(organization: string, pipeline: string) {
        return this.pagerService.getPager({
            key: `PRs/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.pullRequests(organization, pipeline), 25, this),
        });
    }
    /**
     * Adds the latest run to the [@link ActivityService], and sets the latestRun as a mobx computed value.
     *
     * @param {Object} pipelineData Raw data from backend.
     * @return {Object} mapped pipelineData with latestRun set to be a mobx computed value.
     */
    bunkerMapper = (pipelineData) => {
        const data = utils.clone(pipelineData);
        const latestRun = data.latestRun;

        if (latestRun) {
            data.latestRun = this.activityService.setItem(latestRun);
        }

        return data;
    }
    /**
     * Gets a pipeline from the store
     *
     * @param {string} href - Self href of the pipeline.
     * @returns {Object} - Mobx computed value of the pipeline.
     */
    getPipeline(href) {
        return this.getItem(href);
    }


    /**
     * Fetches pipeline from the backend and stores it in
     *
     * @param {string} href - Self href of the pipeline.
     * @param {Object} options
     * @param {boolean} options.useCache - If true fetch from the store if it exists.
     * @returns
     */
    fetchPipeline(href, { useCache } = {}) {
        if (useCache && this.hasItem(href)) {
            return Promise.resolve(this.getItem(href));
        }

        return Fetch.fetchJSON(href).then(data => this.setItem(data));
    }


    /**
     * MobX Action to update the latest run on a pipeline. Use for SSE. This will cause a reaction
     * and rerender anything that uses the latest run of this pipeline.
     *
     * @param {Object} run An activity from activityService.getItem().
     */
    @action
    updateLatestRun(run) {
        const pipeline = this.getItem(run._links.parent.href);
        if (pipeline) {
            pipeline.latestRun = run;
        }
    }
}

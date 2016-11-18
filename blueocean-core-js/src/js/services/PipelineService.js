import { Pager } from './Pager';
import RestPaths from '../paths/rest';
import { Fetch } from '../fetch';
import utils from '../utils';
import { BunkerService } from './BunkerService';
import { action } from 'mobx';
import { Loader } from './Loader';
export class PipelineService extends BunkerService {
    constructor(pagerService, activityService) {
        super(pagerService);
        this.activityService = activityService;
    }


   

    allPipelinesPager() { 
        return this.pagerService.getPager({
            key: 'PipelinesAll',
            lazyPager: () => new Pager(RestPaths.allPipelines(), 25, this),
        });
    }

    organiztionPipelinesPager(organization) {
        return this.pagerService.getPager({
            key: `Pipelines/${organization}`,
            lazyPager: () => new Pager(RestPaths.organizationPipelines(organization), 25, this),
        });
    }

    branchPager(organization: string, pipeline: string) {
        return this.pagerService.getPager({
            key: `Branches/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.branches(organization, pipeline), 25, this),
        });
    }


    prPager(organization: string, pipeline: string) {
        return this.pagerService.getPager({
            key: `PRs/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.pullRequests(organization, pipeline), 25, this),
        });
    }
    bunkerMapper = (pipelineData) => {
        const data = utils.clone(pipelineData);
        const latestRun = data.latestRun;

        const ret = data;
        
        if (latestRun) {
            this.activityService.setLatestActivity(latestRun);
            ret.latestRun = this.activityService.getLatestActivity(latestRun._links.parent.href);
        }

        return data;
    }
    getPipeline(href) {
        return this.getItem(href);
    }


    fetchPipeline(href, { useCache } = {}) {
        if (useCache && this.hasItem(href)) {
            return Promise.resolve(this.getItem(href));
        }

        return Fetch.fetchJSON(href).then(data => this.setItem(data));
    }


    @action
    updateLatestRun(run) {
        const pipeline = this.getItem(run._links.parent.href);
        if (pipeline) {
            pipeline.latestRun = run;
        }
    }
}

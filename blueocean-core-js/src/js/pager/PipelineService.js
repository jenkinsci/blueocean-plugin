import { Pager } from './Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { Fetch } from '../fetch';
import utils from '../utils';
import { BunkerService } from './BunkerService';
import { computed, createTransformer } from 'mobx';
export class PipelineService extends BunkerService {
    constructor(pagerService, activityService) {
        super(pagerService);
        this.activityService = activityService;
    }


   

    allPipelinesPager() {
        console.log('aaaaa');
        return {
            key: 'PipelinesAll',
            lazyPager: () => new Pager(RestPaths.allPipelines(), 25, this),
        };
    }

    organiztionPipelinesPager(organization) {
        return {
            key: `Pipelines/${organization}`,
            lazyPager: () => new Pager(RestPaths.organizationPipelines(organization), 25, this),
        };
    }

    bunkerMapper = (pipelineData) => {
        const data = utils.clone(pipelineData);
        const latestRun = data.latestRun;

        const ret = data;
        
        if (latestRun) {
            ret.getLatestRun = () =>  this.activityService.getOrAddActivity(latestRun);
        }

        return data;
    }
    getPipeline(href) {
        return computed(() => this.getItem(href)).get();
    }
    fetchPipeline(href) {
        return Fetch.fetchJSON(href)
            .then(data => {
                this.setItem(data);
                return data;
            });
    }
}

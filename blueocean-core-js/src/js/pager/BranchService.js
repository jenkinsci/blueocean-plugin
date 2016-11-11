import { observable } from 'mobx';
import { Pager } from './Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { DataBunker } from '../model/DataBunker';
import { BunkerService } from './BunkerService';
import utils from '../utils';
import { action, computed } from 'mobx';
export class BranchService extends BunkerService {
   
    constructor(pagerService, activityService) {
        super(pagerService);
        this.activityService = activityService;

    }
   
    branchPager(organization: string, pipeline: string) {
        return this.pagerService.getPager({
            key: `Branches/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.branches(organization, pipeline), 25, this),
        });
    }
    @action
    updateLatestRun(run) {
        const pipeline = this.getItem(run._links.parent.href);
        if (pipeline) {
            pipeline.latestRun = run;
        }
    }
}

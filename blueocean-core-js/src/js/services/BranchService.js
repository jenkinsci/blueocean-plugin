import { Pager } from './Pager';
import RestPaths from '../paths/rest';
import { BunkerService } from './BunkerService';
import { action } from 'mobx';
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

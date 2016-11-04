// @flow 
import { observable } from 'mobx';
import { Pager } from '../Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { DataBunker } from '../model/DataBunker';

import BranchModel from '../model/BranchModel';
import { Fetch } from '../fetch';
import type PagerService from './PagerService';
import type ActivityService from './PagerService';
import type SSEService from './SSEService';
import type { BranchModelType } from '../model/Types'
import utils from '../utils';

export default class BranchService {
    pagerService: PagerService;
    sseService: SSEService;
    activityService: ActivityService;

    constructor(pagerService: PagerService, sseService: SSEService, activityService: ActivityService) {
        this.pagerService = pagerService;
        this.activityService = activityService;
        this.sseService = sseService;
        this.sseService.registerHandler((event) => this._sseEventHandler(event));
    }
    @observable
    bunker = new DataBunker(this._keyFn, this._mapperFn, this._instanceFn);

    branchPager(organization: string, pipeline: string) {
        return this.pagerService.getPager({
            key: `Branches/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.branches(organization, pipeline), 25, this.bunker),
        });
    }

    _keyFn(item: Object) {
        return item._links.self.href;
    }

    _instanceFn(item: BranchModelType) {
        return new BranchModel(item);
    }
    _mapperFn = (pipelineData: Object) => {
        const data = utils.clone(pipelineData);
        const latestRun = data.latestRun;

        const ret: BranchModelType = data;
        
        if (latestRun) {
            ret.latestRun = this.activityService.getOrAddActivity(latestRun);
        }

        return data;
    }

    _sseEventHandler(event: Object) {
       
    }
}

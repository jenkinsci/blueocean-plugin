// @flow
import { observable } from 'mobx';
import { Pager } from '../Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { DataBunker } from '../model/DataBunker';
import { PipelineModel } from '../model/PipelineModel';
import { Fetch } from '../fetch';
import utils from '../utils';
import type PagerService from './PagerService';
import type SSEService from './SSEService';
import type ActivityService from './ActivityService';
import type { PipelineModelType } from '../model/Types'

export default class PipelineService {
    pagerService : PagerService;
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

    allPipelinesPager() {
        return {
            key: 'PipelinesAll',
            lazyPager: () => new Pager(RestPaths.allPipelines(), 25, this.bunker),
        };
    }

    organiztionPipelinesPager(organization: string) {
        return {
            key: `Pipelines/${organization}`,
            lazyPager: () => new Pager(RestPaths.organizationPipelines(organization), 25, this.bunker),
        };
    }

    _keyFn(item: Object): string {
        return item._links.self.href;
    }

    _instanceFn(item: PipelineModelType): PipelineModel {
        return new PipelineModel(item);
    }


    _mapperFn = (pipelineData: Object) => {
        const data = utils.clone(pipelineData);
        const latestRun = data.latestRun;

        const ret: PipelineModelType = data;
        
        if (latestRun) {
            ret.latestRun = this.activityService.getOrAddActivity(latestRun);
        }

        return data;
    }

    fetchPipelineByHref(href: string) {
        return Fetch.fetchJSON(href)
            .then(data => {
                this.bunker.setItem(data);
                return data;
            });
    }

    _sseEventHandler(event: Object) {
        console.log('_sseEventHandler', event);
        switch (event.jenkins_event) {
        case 'job_crud_created':
            this.fetchPipelineByHref(`${AppPaths.getJenkinsRootURL}/${event.blueocean_job_rest_url}`);
            this.pagerService.invalidatePagerHrefs();
            break;
        case 'job_crud_deleted':
            this.bunker.removeItem(event.blueocean_job_rest_url);
            break;
        case 'job_crud_renamed':
            // TODO: Implement this.
            // Seems to be that SSE fires an updated event for the old job,
            // then a rename for the new one. This is somewhat confusing for us.
            break;
        default :
        // Else ignore the event.
        }
    }
}

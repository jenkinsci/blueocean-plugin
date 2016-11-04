import { observable } from 'mobx';
import { Pager } from '../Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { DataBunker } from '../model/DataBunker';
import { PipelineModel } from '../model/PipelineModel';
import { Fetch } from '../fetch';

export default class PipelineService {
    constructor(pagerService, sseService) {
        this.pagerService = pagerService;
        this.sseService = sseService;
        this.sseService.registerHandler((event) => this._sseEventHandler(event));
    }
    @observable
    bunker = new DataBunker(this._keyFn, this._mapperFn);

    allPipelinesPager() {
        return {
            key: 'PipelinesAll',
            lazyPager: () => new Pager(RestPaths.allPipelines(), 25, this.bunker),
        };
    }

    organiztionPipelinesPager(organization) {
        return {
            key: `Pipelines/${organization}`,
            lazyPager: () => new Pager(RestPaths.organizationPipelines(organization), 25, this.bunker),
        };
    }

    _keyFn(item) {
        return item._links.self.href;
    }

    _mapperFn(item) {
        return new PipelineModel(item);
    }

    fetchPipeline(href) {
        return Fetch.fetchJSON(href)
            .then(data => {
                this.bunker.setItem(data);
                return data;
            });
    }

    _sseEventHandler(event) {
        console.log('_sseEventHandler', event);
        switch (event.jenkins_event) {
        case 'job_crud_created':
            this.fetchPipeline(`${AppPaths.getJenkinsRootURL}/${event.blueocean_job_rest_url}`);
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

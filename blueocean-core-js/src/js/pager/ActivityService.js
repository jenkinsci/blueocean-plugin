import { observable } from 'mobx';
import { Pager } from '../Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { DataBunker } from '../model/DataBunker';
import { ActivityModel } from '../model/ActivityModel';
import { Fetch } from '../fetch';

export default class ActivityService {
    constructor(pagerService, sseService) {
        this.pagerService = pagerService;
        this.sseService = sseService;
        this.sseService.registerHandler((event) => this._sseEventHandler(event));
    }
    @observable
    bunker = new DataBunker(this._keyFn, this._mapperFn);

    activityPager(organization, pipeline) {
        return this.pagerService.getPager({
            key: `Activities/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.activities(organization, pipeline), 25, this.bunker),
        });
    }

    _keyFn(item) {
        return item._links.self.href;
    }

    _mapperFn(item) {
        return new ActivityModel(item);
    }

    _sseEventHandler(event) {
    
    }
}

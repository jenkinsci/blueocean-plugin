import { observable } from 'mobx';
import { Pager } from '../Pager';
import { AppPaths, RestPaths } from '../utils/paths';
import { DataBunker } from '../model/DataBunker';

import BranchModel from '../model/BranchModel';
import { Fetch } from '../fetch';

export default class BranchService {
    constructor(pagerService, sseService) {
        this.pagerService = pagerService;
        this.sseService = sseService;
        this.sseService.registerHandler((event) => this._sseEventHandler(event));
    }
    @observable
    bunker = new DataBunker(this._keyFn, this._mapperFn);

    branchesPager(organization, pipeline) {
        return this.pagerService.getPager({
            key: `Branches/${organization}-${pipeline}`,
            lazyPager: () => new Pager(RestPaths.branches(organization, pipeline), 25, this.bunker),
        });
    }

    _keyFn(item) {
        return item._links.self.href;
    }

    _mapperFn(item) {
     //   return new BranchModel(item, x => this._pipelineModelMapper(x));
    }

    _sseEventHandler(event) {
       
    }
}

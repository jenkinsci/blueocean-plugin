/**
 * Created by cmeyers on 10/6/16.
 */
import { action, observable } from 'mobx';

import { paginate, applyFetchMarkers } from '../util/smart-fetch';
import { paginateUrl } from '../util/UrlUtils';
import UrlConfig from '../config';

export class PipelinesService {

    @observable
    allPipelines = [];

    @action
    fetchAllPipelines() {
        // eslint-disable-next-line max-len
        const url = `${UrlConfig.getRestRoot()}/search/?q=type:pipeline;excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject&filter=no-folders`;
        return paginate({ urlProvider: paginateUrl(url) })
            .then((data) => this._setAllPipelines(data));
    }

    @action
    _setAllPipelines(data) {
        this.allPipelines.replace(data);
        applyFetchMarkers(this.allPipelines, data);
    }

}

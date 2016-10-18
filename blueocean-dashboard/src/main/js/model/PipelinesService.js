/**
 * Created by cmeyers on 10/6/16.
 */
import { action, computed, map, observable } from 'mobx';

import { paginate, applyFetchMarkers } from '../util/smart-fetch';
import { paginateUrl } from '../util/UrlUtils';
import UrlConfig from '../config';

export class PipelinesService {

    @observable
    _selectedOrgName;

    @observable
    _pipelinesByOrganization = map();

    @observable
    _allPipelines = [];

    @computed
    get pipelineList() {
        return this._selectedOrgName ?
            this._organizationList :
            this._allPipelines;
    }

    @computed
    get _organizationList() {
        return this._pipelinesByOrganization.get(this._selectedOrgName);
    }

    setOrganization(orgName) {
        this._selectedOrgName = orgName;
    }

    @action
    fetchPipelines(organizationName) {
        this.setOrganization(organizationName);

        const byType = 'type:pipeline;';
        const byOrg = organizationName ? `organization:${encodeURIComponent(organizationName)};` : '';
        const flatten = 'excludedFromFlattening:jenkins.branch.MultiBranchProject,hudson.matrix.MatrixProject;';
        const filter = '&filter=no-folders';

        // eslint-disable-next-line max-len
        const url = `${UrlConfig.getRestRoot()}/search/?q=${byType}${byOrg}${flatten}${filter}`;
        return paginate({ urlProvider: paginateUrl(url) })
            .then((data) => this._setAllPipelines(organizationName, data));
    }

    @action
    _setAllPipelines(organizationName, data) {
        if (organizationName) {
            this._pipelinesByOrganization.set(organizationName, data);
            const updated = this._pipelinesByOrganization.get(organizationName);
            applyFetchMarkers(updated, data);
            this._pipelinesByOrganization.set(organizationName, updated);
        } else {
            this._allPipelines.replace(data);
            applyFetchMarkers(this._allPipelines, data);
        }
    }

}

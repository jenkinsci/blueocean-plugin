import { observable } from 'mobx';

export class PipelineModel {
    @observable _data = {};

    constructor(pipelineData) {
        this._data = pipelineData;
    }

  
    get name() {
        return this._data.name;
    }

    get fullName() {
        return this._data.fullName;
    }

    get organization() {
        return this._data.organization;
    }
    get numberOfSuccessfulBranches() {
        return this._data.numberOfSuccessfulBranches;
    }

    get numberOfFailingBranches() {
        return this._data.numberOfFailingBranches;
    }

    get numberOfSuccessfulPullRequests() {
        return this._data.numberOfSuccessfulPullRequests;
    }

    get numberOfFailingPullRequests() {
        return this._data.numberOfFailingPullRequests;
    }

    get displayName() {
        return this._data.displayName;
    }
    get weatherScore() {
        return this._data.weatherScore;
    }
    get _links() {
        return this._data._links;
    }

    get _class() {
        return this._data._class;
    }
}

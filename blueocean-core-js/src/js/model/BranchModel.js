// @flow
import { Model } from './Model';
import type { BranchModelType, LinkObject } from './Types';
import type { ActivityModel } from './ActivityModel';
export default class BranchModel extends Model<BranchModelType> {
     get name(): string {
        return this._data.name;
    }

    get fullName(): string {
        return this._data.fullName;
    }

    get organization(): string {
        return this._data.organization;
    }
    get numberOfSuccessfulBranches(): number {
        return this._data.numberOfSuccessfulBranches;
    }

    get numberOfFailingBranches(): number {
        return this._data.numberOfFailingBranches;
    }

    get numberOfSuccessfulPullRequests(): number {
        return this._data.numberOfSuccessfulPullRequests;
    }

    get numberOfFailingPullRequests(): number{
        return this._data.numberOfFailingPullRequests;
    }

    get displayName(): string {
        return this._data.displayName;
    }
    get weatherScore(): string {
        return this._data.weatherScore;
    }
    get _links(): LinkObject {
        return this._data._links;
    }

    get _class(): string {
        return this._data._class;
    }
    get pullRequest(): Object {
        return this._data.pullRequest;
    }

    get latestRun(): ActivityModel {
        return this._data.latestRun;
    }
}

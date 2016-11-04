// @flow
import { observable } from 'mobx';
import { Model } from './Model';
import type { ActivityModelType, LinkObject } from './Types';
export class ActivityModel extends Model<ActivityModelType> {

    get organization(): string {
        return this._data.organization;
    }
    get pipeline(): string {
        return this._data.pipeline;
    }

    get _links(): LinkObject {
        return this._data._links;
    }

    get changeSet(): Object {
        return this._data.changeSet;
    }

    get durationInMillis(): number {
        return this._data.durationInMillis;
    }


    get estimatedDurationInMillis(): number {
        return this._data.estimatedDurationInMillis;
    }

    get id(): string {
        return this._data.id;
    }

    get result(): string {
        return this._data.result;
    }

    get state(): string {
        return this._data.state;
    }

    get startTime(): string {
        return this._data.startTime;
    }

    get endTime(): string {
        return this._data.endTime;
    }

    get commitId(): string {
        return this._data.commitId;
    }
}

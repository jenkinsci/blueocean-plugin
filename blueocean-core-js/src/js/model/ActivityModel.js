import { observable } from 'mobx';
import Model from './Model';
export class ActivityModel extends Model {

    get organization() {
        return this._data.organization;
    }
    get pipeline() {
        return this._data.pipeline;
    }

    get _links() {
        return this._data._links;
    }

    get changeSet() {
        return this._data.changeSet;
    }

    get durationInMillis() {
        return this._data.durationInMillis;
    }


    get estimatedDurationInMillis() {
        return this._data.estimatedDurationInMillis;
    }

    get pipeline() {
        return this._data.pipeline;
    }

    get id() {
        return this._data.id;
    }

    get result() {
        return this._data.result;
    }

    get state() {
        return this._data.state;
    }

    get startTime() {
        return this._data.startTime;
    }

    get endTime() {
        return this._data.endTime;
    }

    get commitId() {
        return this._data.commitId;
    }
}

import { Record } from 'immutable';
import { PipelineRecord } from '../components/records';
export const State = Record({
    pipelines: null,
    pipeline: PipelineRecord,
    runs: null,
    isFetching: false,
});

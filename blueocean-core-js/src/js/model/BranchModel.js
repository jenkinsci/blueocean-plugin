import Model from './Model';
import PipelineModel from './PipelineModel';

console.log('model', Model);
export default class BranchModel extends Model {
  

    get pullRequest() {
        return this._data.pullRequest;
    }
}

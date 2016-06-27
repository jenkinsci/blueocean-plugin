import { assert } from 'chai';

import Pipeline from '../../main/js/api/Pipeline';
import Branch, { fromSSEEvent } from '../../main/js/api/Branch';

describe('Branch', () => {

    it('equals', () => {
        const pipeline_1 = new Pipeline('jenkins', 'pipeline1');
        const pipeline_2 = new Pipeline('jenkins', 'pipeline2');
        const branch_1_1 = new Branch(pipeline_1, 'branch_1');
        const branch_1_2 = new Branch(pipeline_1, 'branch_2');
        const branch_2_1 = new Branch(pipeline_2, 'branch_1');

        // same branch, different pipeline
        assert.equal(false, branch_1_1.equals(branch_1_2));
        // same pipeline, different branch
        assert.equal(false, branch_1_1.equals(branch_2_1));
        // same pipeline, same branch
        assert.equal(true, branch_1_1.equals(new Branch(pipeline_1, 'branch_1')));
    });

});

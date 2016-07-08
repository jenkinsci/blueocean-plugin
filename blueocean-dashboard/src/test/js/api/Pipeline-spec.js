import { assert } from 'chai';

import Pipeline from '../../../main/js/api/Pipeline';
import Branch from '../../../main/js/api/Branch';
import config from '../../../main/js/config';

config.blueoceanAppURL = 'blue';

describe('Pipeline', () => {

    const pipeline_1 = new Pipeline('jenkins', 'pipeline1');
    const branch_1_1 = new Branch('jenkins', 'pipeline1', 'branch_1');
    const branch_1_2 = new Branch('jenkins', 'pipeline1', 'branch_2');
    const branch_2_1 = new Branch('jenkins', 'pipeline2', 'branch_1');

    it('equals', () => {
        // Pipeline and Branch refs should fail, even for Branches
        // on the same pipeline.
        assert.equal(false, branch_1_1.equals(pipeline_1));
        assert.equal(false, pipeline_1.equals(branch_1_1));

        // same branch, different pipeline
        assert.equal(false, branch_1_1.equals(branch_1_2));
        // same pipeline, different branch
        assert.equal(false, branch_1_1.equals(branch_2_1));
        // same pipeline, same branch
        assert.equal(true, branch_1_1.equals(new Branch('jenkins', 'pipeline1', 'branch_1')));
    });

    it('runDetailsRouteUrl', () => {
        // The pipeline name is inserted in the url as the branch name for
        // non-multi-branch pipelines i.e. 'pipeline1'.
        assert.equal('/organizations/jenkins/pipeline1/detail/pipeline1/3/pipeline', pipeline_1.runDetailsRouteUrl('3'));
        // The branch name is inserted in the url as the branch name for
        // multi-branch pipelines i.e. 'branch_1'.
        assert.equal('/organizations/jenkins/pipeline1/detail/branch_1/3/pipeline', branch_1_1.runDetailsRouteUrl('3'));
    });

    it('restUrl', () => {
        assert.equal('blue/rest/organizations/jenkins/pipelines/pipeline1', pipeline_1.restUrl('3'));
        assert.equal('blue/rest/organizations/jenkins/pipelines/pipeline1/branches/branch_1', branch_1_1.restUrl('3'));
    });
});

/**
 * Created by cmeyers on 9/9/16.
 */
import { assert } from 'chai';

import rest from '../../../src/js/paths/rest';

describe('Rest Paths', () => {
    describe('generates the correct path for - ', () => {
        it('apiRoot', () => {
            assert.equal(rest.apiRoot(), '/blue/rest');
        });

        it('run', () => {
            const runUrl = rest.run({
                organization: 'jenkins',
                pipeline: 'some/pipeline in/a_folder',
                branch: 'feature/2085',
                runId: 30,
            });

            assert.equal(runUrl, '/blue/rest/organizations/jenkins/pipelines/some/pipelines/pipeline in/pipelines/a_folder/branches/feature%252F2085/runs/30/');
        });
    });
});

import { assert } from 'chai';

import Utils from '../../src/js/utils';

const partUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines';
const fullUrl = `${partUrl}/`;

describe('Utils', () => {
    describe('cleanSlashes', () => {
        it('removes double slashes', () => {
            const testUrl = '/jenkins//blue//rest/organizations/jenkins/pipelines';
            assert.equal(Utils.cleanSlashes(testUrl), fullUrl);
        });
        it('adds trailing slash', () => {
            const testUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines';
            assert.equal(Utils.cleanSlashes(testUrl), fullUrl);
        });
        it('removes triple slashes', () => {
            const testUrl = '/jenkins///blue/rest/organizations/jenkins/pipelines';
            assert.equal(Utils.cleanSlashes(testUrl), fullUrl);
        });
        it('wont add a slash when opting out', () => {
            const testUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines';
            assert.equal(Utils.cleanSlashes(testUrl, false), partUrl);
        });
    });
});

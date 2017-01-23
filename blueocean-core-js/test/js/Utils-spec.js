import { assert } from 'chai';

import Utils from '../../src/js/utils';

const missingSlashUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines';
const cleanUrl = `${missingSlashUrl}/`;
const params = 'foo=bar&bar=foo';

describe('Utils', () => {
    describe('cleanSlashes', () => {
        it('does not modify a clean url', () => {
            assert.equal(Utils.cleanSlashes(cleanUrl), cleanUrl);
        });
        it('does not modify a clean url w/ query params', () => {
            const cleanUrlWithParams = `${cleanUrl}?${params}`;
            assert.equal(Utils.cleanSlashes(cleanUrlWithParams), cleanUrlWithParams);
        });
        it('removes double slashes', () => {
            const testUrl = '/jenkins//blue//rest/organizations/jenkins/pipelines';
            assert.equal(Utils.cleanSlashes(testUrl), cleanUrl);
        });
        it('removes triple slashes', () => {
            const testUrl = '/jenkins///blue/rest/organizations/jenkins/pipelines';
            assert.equal(Utils.cleanSlashes(testUrl), cleanUrl);
        });
        it('adds trailing slash', () => {
            const testUrl = '/jenkins/blue/rest/organizations/jenkins/pipelines';
            assert.equal(Utils.cleanSlashes(testUrl), cleanUrl);
        });
        it('adds the slash before query params', () => {
            const testUrl = `${missingSlashUrl}?${params}`;
            assert.equal(Utils.cleanSlashes(testUrl), `${missingSlashUrl}/?${params}`);
        });
    });
});

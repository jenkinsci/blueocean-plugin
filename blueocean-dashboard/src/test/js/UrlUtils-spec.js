import { assert } from 'chai';

import { buildRunDetailsUrl } from '../../main/js/util/UrlUtils';

describe('UrlUtils', () => {
    describe('buildRunDetailsUrl', () => {
        it('should build the baseUrl if tabName ommitted', () => {
            const url = buildRunDetailsUrl(
                'jenkins',
                'blueocean',
                'master',
                1
            );

            assert.equal(url, '/organizations/jenkins/blueocean/detail/master/1');
        });
        it('should build the full url with tab name', () => {
            const url = buildRunDetailsUrl(
                'jenkins',
                'blueocean',
                'master',
                1,
                'changes'
            );

            assert.equal(url, '/organizations/jenkins/blueocean/detail/master/1/changes');
        });
        it('should escape characters correctly', () => {
            const url = buildRunDetailsUrl(
                'jenkins',
                'blueocean',
                'feature/JENKINS-666',
                1,
                'changes'
            );

            assert.equal(url, '/organizations/jenkins/blueocean/detail/feature%2FJENKINS-666/1/changes');
        });
    });
});

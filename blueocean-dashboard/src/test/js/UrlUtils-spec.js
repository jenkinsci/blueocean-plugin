import { assert } from 'chai';

import {
    buildOrganizationUrl,
    buildPipelineUrl,
    buildRunDetailsUrl,
} from '../../main/js/util/UrlUtils';

describe('UrlUtils', () => {
    describe('buildOrganizationUrl', () => {
        it('should build the proper url', () => {
            const url = buildOrganizationUrl(
                'jenkins'
            );

            assert.equal(url, '/organizations/jenkins');
        });
    });
    describe('buildPipelineUrl', () => {
        it('should build the baseUrl for top level pipeline', () => {
            const url = buildPipelineUrl(
                'jenkins',
                'blueocean'
            );

            assert.equal(url, '/organizations/jenkins/blueocean');
        });
        it('should build the baseUrl for 1-level nested pipeline', () => {
            const url = buildPipelineUrl(
                'jenkins',
                'folder/blueocean',
            );

            assert.equal(url, '/organizations/jenkins/folder%2Fblueocean');
        });
        it('should build the baseUrl for 3-level nested pipeline', () => {
            const url = buildPipelineUrl(
                'jenkins',
                'folder1/folder2/folder3/blueocean',
            );

            assert.equal(url, '/organizations/jenkins/folder1%2Ffolder2%2Ffolder3%2Fblueocean');
        });
    });

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

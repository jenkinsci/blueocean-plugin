import { assert } from 'chai';

import {
    buildOrganizationUrl,
    buildPipelineUrl,
    calculateRunLogURLObject,
    calculateStepsBaseUrl,
    calculateLogUrl,
    calculateNodeBaseUrl,
    buildClassicConfigUrl,
    toClassicJobPage,
} from '../../src/js/utils/UrlUtils';

describe('UrlUtils', () => {
    describe('calculate log url', () => {
        it('should build the logUrl with only url set', () => {
            const testUrl = '/some/thing/';
            const url = calculateLogUrl({
                url: testUrl,
            });

            assert.equal(url, testUrl);
        });
        it('should build the logUrl with node set', () => {
            const testUrl = '/some/thing/';
            const url = calculateLogUrl({
                nodesBaseUrl: testUrl,
                node: {
                    id: '1',
                },
            });

            assert.equal(url, `${testUrl}/1/log/`);
        });
    });
    describe('calculate calculateNodeBaseUrl', () => {
        const testData = {
            _appURLBase: '/some/thing',
            name: 'xxx',
            branch: 'karaoke',
            runId: 7,
            organization: 'jenkins',
        };
        it('should build the url no multibranch', () => {
            const url = calculateNodeBaseUrl(testData);

            assert.equal(url, `${testData._appURLBase}/rest/organizations/jenkins/` + `pipelines/${testData.name}/runs/${testData.runId}/nodes/`);
        });
        it('should build the url with multibranch', () => {
            const isMultiBranch = true;
            const url = calculateNodeBaseUrl({ ...testData, isMultiBranch });

            assert.equal(
                url,
                `${testData._appURLBase}/rest/organizations/jenkins/` + `pipelines/${testData.name}/branches/${testData.branch}/runs/${testData.runId}/nodes/`
            );
        });
    });

    describe('double encode branch name in nodeBaseUrl', () => {
        const testData = {
            _appURLBase: '/some/thing',
            name: 'xxx',
            branch: 'feature/test#1',
            runId: 7,
            isMultiBranch: true,
            organization: 'jenkins',
        };
        it('should build the url multibranch', () => {
            const url = calculateNodeBaseUrl(testData);

            assert.equal(
                url,
                `${testData._appURLBase}/rest/organizations/jenkins/` +
                    `pipelines/${testData.name}/branches/feature%252Ftest%25231/runs/${testData.runId}/nodes/`
            );
        });
    });

    describe('calculate calculateStepsBaseUrl', () => {
        const testData = {
            _appURLBase: '/some/thing',
            name: 'xxx',
            branch: 'karaoke',
            runId: 7,
            organization: 'jenkins',
        };
        it('should build the url with node', () => {
            const node = 15;
            const url = calculateStepsBaseUrl({ ...testData, node });
            assert.equal(url, `${testData._appURLBase}/rest/organizations/jenkins/` + `pipelines/${testData.name}/runs/${testData.runId}/nodes/${node}/steps/`);
        });
        it('should build the url with multibranch and no node', () => {
            const isMultiBranch = true;
            const url = calculateStepsBaseUrl({ ...testData, isMultiBranch });

            assert.equal(
                url,
                `${testData._appURLBase}/rest/organizations/jenkins/` + `pipelines/${testData.name}/branches/${testData.branch}/runs/${testData.runId}/steps/`
            );
        });
    });
    describe('calculate calculateRunLogURLObject', () => {
        const testData = {
            _appURLBase: '/some/thing',
            name: 'xxx',
            branch: 'karaoke',
            runId: 7,
            organization: 'jenkins',
        };
        it('should build the urlObject no multibranch', () => {
            const urlObject = calculateRunLogURLObject(testData);

            assert.equal(urlObject.url, `${testData._appURLBase}/rest/organizations/jenkins/` + `pipelines/${testData.name}/runs/${testData.runId}/log/`);
            assert.equal(urlObject.fileName, `${testData.runId}.txt`);
        });
        it('should build the urlObject with multibranch', () => {
            const isMultiBranch = true;
            const urlObject = calculateRunLogURLObject({ ...testData, isMultiBranch });

            assert.equal(
                urlObject.url,
                `${testData._appURLBase}/rest/organizations/jenkins/` + `pipelines/${testData.name}/branches/${testData.branch}/runs/${testData.runId}/log/`
            );
            assert.equal(urlObject.fileName, `${testData.branch}-${testData.runId}.txt`);
        });
    });
});

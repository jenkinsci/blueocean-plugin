import { assert } from 'chai';

import {
    buildOrganizationUrl, buildPipelineUrl, buildRunDetailsUrl,
    calculateRunLogURLObject, calculateStepsBaseUrl, calculateLogUrl, calculateNodeBaseUrl,
    buildClassicConfigUrl,
} from '@jenkins-cd/blueocean-core-js';

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
        it('should build the baseUrl if tabName omitted', () => {
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
    describe('calculate log url', () => {
        it('should build the logUrl with only url set', () => {
            const testUrl = '/some/thing/';
            const url = calculateLogUrl(
                {
                    url: testUrl,
                }
            );

            assert.equal(url, testUrl);
        });
        it('should build the logUrl with node set', () => {
            const testUrl = '/some/thing/';
            const url = calculateLogUrl(
                {
                    nodesBaseUrl: testUrl,
                    node: {
                        id: '1'
                    }
                }
            );

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

            assert.equal(url, `${testData._appURLBase}/rest/organizations/jenkins/` +
                `pipelines/${testData.name}/runs/${testData.runId}/nodes/`);
        });
        it('should build the url with multibranch', () => {
            const isMultiBranch = true;
            const url = calculateNodeBaseUrl({...testData, isMultiBranch});

            assert.equal(url, `${testData._appURLBase}/rest/organizations/jenkins/` +
                `pipelines/${testData.name}/branches/${testData.branch}/runs/${testData.runId}/nodes/`);
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

            assert.equal(url, `${testData._appURLBase}/rest/organizations/jenkins/` +
                `pipelines/${testData.name}/branches/feature%252Ftest%25231/runs/${testData.runId}/nodes/`);
        });
    });

    describe('build classicConfigUrl', () => {

        const testData = {
            fullName : 'foldey/nesty/woozle%20wozzle/mazzig'
        };

        it('should build the url for classic config', () => {
            const url = buildClassicConfigUrl(testData);
            assert.equal(url, '/jenkins/job/foldey/job/nesty/job/woozle%20wozzle/job/mazzig/configure');
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
            const url = calculateStepsBaseUrl({...testData, node});
            assert.equal(url, `${testData._appURLBase}/rest/organizations/jenkins/` +
                `pipelines/${testData.name}/runs/${testData.runId}/nodes/${node}/steps/`);
        });
        it('should build the url with multibranch and no node', () => {
            const isMultiBranch = true;
            const url = calculateStepsBaseUrl({...testData, isMultiBranch});

            assert.equal(url, `${testData._appURLBase}/rest/organizations/jenkins/` +
                `pipelines/${testData.name}/branches/${testData.branch}/runs/${testData.runId}/steps/`);
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

            assert.equal(urlObject.url, `${testData._appURLBase}/rest/organizations/jenkins/` +
                `pipelines/${testData.name}/runs/${testData.runId}/log/`);
            assert.equal(urlObject.fileName, `${testData.runId}.txt`);
        });
        it('should build the urlObject with multibranch', () => {
            const isMultiBranch = true;
            const urlObject = calculateRunLogURLObject({...testData, isMultiBranch});

            assert.equal(urlObject.url, `${testData._appURLBase}/rest/organizations/jenkins/` +
                `pipelines/${testData.name}/branches/${testData.branch}/runs/${testData.runId}/log/`);
            assert.equal(urlObject.fileName, `${testData.branch}-${testData.runId}.txt`);
        });
    });
});
;

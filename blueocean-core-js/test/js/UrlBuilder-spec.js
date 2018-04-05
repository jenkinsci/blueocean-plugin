/**
 * Created by cmeyers on 8/25/16.
 */
import { assert } from 'chai';

import { UrlBuilder } from '../../src/js/';
import { buildClassicConfigUrl, toClassicJobPage } from '../../src/js/utils/UrlUtils';

const createObjectFromLink = url => ({ _links: { self: { href: url } } });

describe('UrlBuilder', () => {
    describe('buildRunUrlForRestUrl', () => {
        it('throws sensible error for bad url', () => {
            assert.throws(() => UrlBuilder.buildRunUrlForRestUrl('/a/b/c/d'), 'Could not extract URI components');
        });

        it('handles a URL', () => {
            const freestyle = '/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/';
            const url = UrlBuilder.buildRunUrlForRestUrl(freestyle);
            assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
        });
    });

    describe('buildRunUrlForDetails', () => {
        describe('handling input', () => {
            it('handles an object', () => {
                const freestyle = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/');

                const url = UrlBuilder.buildRunUrlForDetails(freestyle);
                assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
            });

            it('throws sensible error for bad url', () => {
                assert.throws(() => UrlBuilder.buildRunUrlForDetails(createObjectFromLink('/a/b/c/d')), 'Could not extract URI components');
            });
        });

        describe('freestyle', () => {
            it('handles top-level job', () => {
                const freestyle = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/');

                const url = UrlBuilder.buildRunUrlForDetails(freestyle);
                assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
            });

            it('handles nested job', () => {
                const freestyle = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/myfolder/pipelines/pipeline-2/runs/103/');

                const url = UrlBuilder.buildRunUrlForDetails(freestyle);
                assert.equal(url, '/organizations/jenkins/myfolder%2Fpipeline-2/detail/pipeline-2/103/pipeline');
            });
        });

        describe('pipeline', () => {
            it('handles top-level job', () => {
                const pipeline = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/pipeline-failure-15s/runs/42/');

                const url = UrlBuilder.buildRunUrlForDetails(pipeline);
                assert.equal(url, '/organizations/jenkins/pipeline-failure-15s/detail/pipeline-failure-15s/42/pipeline');
            });

            it('handles nested job', () => {
                const pipeline = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/pipeline-failure-15s/runs/42/');

                const url = UrlBuilder.buildRunUrlForDetails(pipeline);
                assert.equal(url, '/organizations/jenkins/pipeline-failure-15s/detail/pipeline-failure-15s/42/pipeline');
            });
        });

        describe('multibranch pipeline', () => {
            it('handles top-level job', () => {
                const multibranch = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/jdl1/branches/experiment%252Fbuild-locally-docker/runs/41/'
                );

                const url = UrlBuilder.buildRunUrlForDetails(multibranch);
                assert.equal(url, '/organizations/jenkins/jdl1/detail/experiment%2Fbuild-locally-docker/41/pipeline');
            });

            it('handles nested job', () => {
                const multibranch = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines' +
                        '/jdl-2/branches/experiment%252Fbuild-locally-docker/runs/21/'
                );

                const url = UrlBuilder.buildRunUrlForDetails(multibranch);
                assert.equal(url, '/organizations/jenkins/folder1%2Ffolder2%2Ffolder3%2Fjdl-2/detail/experiment%2Fbuild-locally-docker/21/pipeline');
            });
        });
    });

    describe('buildRunUrl', () => {
        it('should build the baseUrl if tabName null', () => {
            const url = UrlBuilder.buildRunUrl('jenkins', 'blueocean', 'master', 1, null);

            assert.equal(url, '/organizations/jenkins/blueocean/detail/master/1');
        });

        it('should build the full url with tab name', () => {
            const url = UrlBuilder.buildRunUrl('jenkins', 'blueocean', 'master', 1, 'changes');

            assert.equal(url, '/organizations/jenkins/blueocean/detail/master/1/changes');
        });

        it('should escape characters correctly', () => {
            const url = UrlBuilder.buildRunUrl('jenkins', 'blueocean', 'feature/JENKINS-666', 1, 'changes');

            assert.equal(url, '/organizations/jenkins/blueocean/detail/feature%2FJENKINS-666/1/changes');
        });
    });

    describe('buildOrganizationUrl', () => {
        it('should build the proper url', () => {
            const url = UrlBuilder.buildOrganizationUrl('jenkins');

            assert.equal(url, '/organizations/jenkins');
        });

        it('should encode the url', () => {
            const url = UrlBuilder.buildOrganizationUrl('foo/bar');

            assert.equal(url, '/organizations/foo%2Fbar');
        });
    });

    describe('buildPipelineUrl', () => {
        it('should build the baseUrl for top level pipeline', () => {
            const url = UrlBuilder.buildPipelineUrl('jenkins', 'blueocean');

            assert.equal(url, '/organizations/jenkins/blueocean');
        });

        it('should build the baseUrl for 1-level nested pipeline', () => {
            const url = UrlBuilder.buildPipelineUrl('jenkins', 'folder/blueocean');

            assert.equal(url, '/organizations/jenkins/folder%2Fblueocean');
        });

        it('should build the baseUrl for 3-level nested pipeline', () => {
            const url = UrlBuilder.buildPipelineUrl('jenkins', 'folder1/folder2/folder3/blueocean');

            assert.equal(url, '/organizations/jenkins/folder1%2Ffolder2%2Ffolder3%2Fblueocean');
        });
    });

    describe('build classicConfigUrl', () => {
        const testData = {
            fullName: 'foldey/nesty/woozle%20wozzle/mazzig',
        };

        it('should build the url for classic config', () => {
            const url = UrlBuilder.buildClassicConfigUrl(testData);
            assert.equal(url, '/jenkins/job/foldey/job/nesty/job/woozle%20wozzle/job/mazzig/configure');
        });
    });

    describe('toClassicJobPage', () => {
        it('Supports Non Multibranch', () => {
            assert.equal(UrlBuilder.toClassicJobPage('/jenkins/blue/organizations/jenkins/freestyleA/detail/freestyleA/activity', false), '/job/freestyleA');
            assert.equal(
                UrlBuilder.toClassicJobPage('/jenkins/blue/organizations/jenkins/freestyleA/detail/freestyleA/2/pipeline', false),
                '/job/freestyleA/2'
            );

            // In a folder
            assert.equal(UrlBuilder.toClassicJobPage('/jenkins/blue/organizations/jenkins/Foo%2FBar/activity', false), '/job/Foo/job/Bar');
            assert.equal(UrlBuilder.toClassicJobPage('/jenkins/blue/organizations/jenkins/Foo%2FBar/detail/Bar/1/pipeline', false), '/job/Foo/job/Bar/1');
        });

        it('Supports Multibranch', () => {
            // A job down in a folder
            assert.equal(
                UrlBuilder.toClassicJobPage('/jenkins/blue/organizations/jenkins/folder1%2Ffolder2%2FATH/activity', true),
                '/job/folder1/job/folder2/job/ATH'
            );
            assert.equal(
                UrlBuilder.toClassicJobPage('/jenkins/blue/organizations/jenkins/folder1%2Ffolder2%2FATH/detail/master/1/pipeline', true),
                '/job/folder1/job/folder2/job/ATH/job/master/1'
            );
        });
    });

    describe('buildRestUrl', () => {
        it('works in the happy cases', () => {
            assert.equal(UrlBuilder.buildRestUrl('orgName'), '/jenkins/blue/rest/organizations/orgName/');
            assert.equal(UrlBuilder.buildRestUrl('orgName', 'pipeline'), '/jenkins/blue/rest/organizations/orgName/pipelines/pipeline/');
            assert.equal(
                UrlBuilder.buildRestUrl('orgName', 'pipeline', 'branchName'),
                '/jenkins/blue/rest/organizations/orgName/pipelines/pipeline/branches/branchName/'
            );
            assert.equal(
                UrlBuilder.buildRestUrl('orgName', 'pipeline', 'branchName', '75'),
                '/jenkins/blue/rest/organizations/orgName/pipelines/pipeline/branches/branchName/runs/75/'
            );
            assert.equal(UrlBuilder.buildRestUrl('orgName', 'pipeline', null, '75'), '/jenkins/blue/rest/organizations/orgName/pipelines/pipeline/runs/75/');
        });

        it('escapes like Clint Eastwood', () => {
            // ensure team folder is encoded
            assert.equal(UrlBuilder.buildRestUrl('division/teamName'), '/jenkins/blue/rest/organizations/division%2FteamName/');

            // ensure pipeline folder hierarchy isn't encoded
            assert.equal(UrlBuilder.buildRestUrl('orgName', 'f1/f2/pipeline'), '/jenkins/blue/rest/organizations/orgName/pipelines/f1/f2/pipeline/');

            // Ensure the branch name is encoded twice, because reasons
            assert.equal(
                UrlBuilder.buildRestUrl('orgName', 'pipeline', 'feature/branchName'),
                '/jenkins/blue/rest/organizations/orgName/pipelines/pipeline/branches/feature%252FbranchName/'
            );

            // Ensure run id is encoded for sanity, even though it is currently just a sequence number
            assert.equal(
                UrlBuilder.buildRestUrl('orgName', 'pipeline', 'branchName', 'xx/yy'),
                '/jenkins/blue/rest/organizations/orgName/pipelines/pipeline/branches/branchName/runs/xx%2Fyy/'
            );
        });
    });
});

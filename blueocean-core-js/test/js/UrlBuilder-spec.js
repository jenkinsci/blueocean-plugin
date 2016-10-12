/**
 * Created by cmeyers on 8/25/16.
 */
import { assert } from 'chai';

import { buildRunDetailsUrl, buildRunDetailsUrlFromQueue } from '../../src/js/UrlBuilder';

const createObjectFromLink = (url) => (
    { _links: { self: { href: url } } }
);

describe('UrlBuilder', () => {
    describe('buildRunDetailsUrl', () => {
        describe('handling input', () => {
            it('handles an object', () => {
                const freestyle = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/'
                );

                const url = buildRunDetailsUrl(freestyle);
                assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
            });

            it('handles a URL', () => {
                const freestyle = '/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/';
                const url = buildRunDetailsUrl(freestyle);
                assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
            });

            it('throws sensible error for bogus object', () => {
                assert.throws(() => buildRunDetailsUrl({}), 'Could not find input URL');
            });

            it('throws sensible error for bad url', () => {
                assert.throws(() => buildRunDetailsUrl('/a/b/c/d'), 'Could not extract URI components');
            });

            it('throws sensible error for a string of chars', () => {
                assert.throws(() => buildRunDetailsUrl('abcdefghijklmnopqrstuvwxyz'), 'Could not extract URI components');
            });
        });

        describe('freestyle', () => {
            it('handles top-level job', () => {
                const freestyle = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/'
                );

                const url = buildRunDetailsUrl(freestyle);
                assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
            });

            it('handles nested job', () => {
                const freestyle = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/myfolder/pipelines/pipeline-2/runs/103/'
                );

                const url = buildRunDetailsUrl(freestyle);
                assert.equal(url, '/organizations/jenkins/myfolder%2Fpipeline-2/detail/pipeline-2/103/pipeline');
            });
        });

        describe('pipeline', () => {
            it('handles top-level job', () => {
                const pipeline = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/pipeline-failure-15s/runs/42/'
                );

                const url = buildRunDetailsUrl(pipeline);
                assert.equal(url, '/organizations/jenkins/pipeline-failure-15s/detail/pipeline-failure-15s/42/pipeline');
            });

            it('handles nested job', () => {
                const pipeline = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/pipeline-failure-15s/runs/42/'
                );

                const url = buildRunDetailsUrl(pipeline);
                assert.equal(url, '/organizations/jenkins/pipeline-failure-15s/detail/pipeline-failure-15s/42/pipeline');
            });
        });

        describe('multibranch pipeline', () => {
            it('handles top-level job', () => {
                const multibranch = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/jdl1/branches/experiment%252Fbuild-locally-docker/runs/41/'
                );

                const url = buildRunDetailsUrl(multibranch);
                assert.equal(url, '/organizations/jenkins/jdl1/detail/experiment%2Fbuild-locally-docker/41/pipeline');
            });

            it('handles nested job', () => {
                const multibranch = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines' +
                    '/jdl-2/branches/experiment%252Fbuild-locally-docker/runs/21/'
                );

                const url = buildRunDetailsUrl(multibranch);
                assert.equal(url, '/organizations/jenkins/folder1%2Ffolder2%2Ffolder3%2Fjdl-2/detail/experiment%2Fbuild-locally-docker/21/pipeline');
            });
        });
    });

    describe('buildRunDetailsUrlFromQueue', () => {
        describe('freestyle', () => {
            it('handles top-level job', () => {
                const freestyle = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/queue/28/'
                );

                const url = buildRunDetailsUrlFromQueue(freestyle, false, 20);
                assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/20/pipeline');
            });

            it('handles nested job', () => {
                const freestyle = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/myfolder/pipelines/pipeline-2/queue/103/'
                );

                const url = buildRunDetailsUrlFromQueue(freestyle, false, 40);
                assert.equal(url, '/organizations/jenkins/myfolder%2Fpipeline-2/detail/pipeline-2/40/pipeline');
            });
        });

        describe('pipeline', () => {
            it('handles top-level job', () => {
                const pipeline = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/pipeline-failure-15s/queue/42/'
                );

                const url = buildRunDetailsUrlFromQueue(pipeline, false, 15);
                assert.equal(url, '/organizations/jenkins/pipeline-failure-15s/detail/pipeline-failure-15s/15/pipeline');
            });

            it('handles nested job', () => {
                const pipeline = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/simple-pipeline-jobs/pipelines/pipeline-failure-1m/queue/17/'
                );

                const url = buildRunDetailsUrlFromQueue(pipeline, false, 25);
                assert.equal(url, '/organizations/jenkins/simple-pipeline-jobs%2Fpipeline-failure-1m/detail/pipeline-failure-1m/25/pipeline');
            });
        });

        describe('multibranch pipeline', () => {
            it('handles top-level job', () => {
                const multibranch = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/jdl1/pipelines/experiment%252Fbuild-locally-docker/queue/42/'
                );

                const url = buildRunDetailsUrlFromQueue(multibranch, true, 55);
                assert.equal(url, '/organizations/jenkins/jdl1/detail/experiment%2Fbuild-locally-docker/55/pipeline');
            });

            it('handles nested job', () => {
                const multibranch = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/jdl-2/pipelines/master/queue/31/'
                );

                const url = buildRunDetailsUrlFromQueue(multibranch, true, 11);
                assert.equal(url, '/organizations/jenkins/folder1%2Ffolder2%2Ffolder3%2Fjdl-2/detail/master/11/pipeline');
            });
        });
    });
});

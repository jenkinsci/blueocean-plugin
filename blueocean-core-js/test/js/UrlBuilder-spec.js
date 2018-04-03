/**
 * Created by cmeyers on 8/25/16.
 */
import { assert } from 'chai';

import { badName001, badName002, badName003 } from '../../src/js/UrlBuilder';

const createObjectFromLink = url => ({ _links: { self: { href: url } } });

describe('UrlBuilder', () => {
    describe('badName002', () => {
        it('throws sensible error for bad url', () => {
            assert.throws(() => badName002('/a/b/c/d'), 'Could not extract URI components');
        });

        it('handles a URL', () => {
            const freestyle = '/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/';
            const url = badName002(freestyle);
            assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
        });
    });

    describe('badName001', () => {
        describe('handling input', () => {
            it('handles an object', () => {
                const freestyle = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/');

                const url = badName001(freestyle);
                assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
            });

            it('throws sensible error for bad url', () => {
                assert.throws(() => badName001(createObjectFromLink('/a/b/c/d')), 'Could not extract URI components');
            });
        });

        describe('freestyle', () => {
            it('handles top-level job', () => {
                const freestyle = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/freestyle-success-10m/runs/28/');

                const url = badName001(freestyle);
                assert.equal(url, '/organizations/jenkins/freestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
            });

            it('handles nested job', () => {
                const freestyle = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/myfolder/pipelines/pipeline-2/runs/103/');

                const url = badName001(freestyle);
                assert.equal(url, '/organizations/jenkins/myfolder%2Fpipeline-2/detail/pipeline-2/103/pipeline');
            });
        });

        describe('pipeline', () => {
            it('handles top-level job', () => {
                const pipeline = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/pipeline-failure-15s/runs/42/');

                const url = badName001(pipeline);
                assert.equal(url, '/organizations/jenkins/pipeline-failure-15s/detail/pipeline-failure-15s/42/pipeline');
            });

            it('handles nested job', () => {
                const pipeline = createObjectFromLink('/blue/rest/organizations/jenkins/pipelines/pipeline-failure-15s/runs/42/');

                const url = badName001(pipeline);
                assert.equal(url, '/organizations/jenkins/pipeline-failure-15s/detail/pipeline-failure-15s/42/pipeline');
            });
        });

        describe('multibranch pipeline', () => {
            it('handles top-level job', () => {
                const multibranch = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/jdl1/branches/experiment%252Fbuild-locally-docker/runs/41/'
                );

                const url = badName001(multibranch);
                assert.equal(url, '/organizations/jenkins/jdl1/detail/experiment%2Fbuild-locally-docker/41/pipeline');
            });

            it('handles nested job', () => {
                const multibranch = createObjectFromLink(
                    '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines' +
                        '/jdl-2/branches/experiment%252Fbuild-locally-docker/runs/21/'
                );

                const url = badName001(multibranch);
                assert.equal(url, '/organizations/jenkins/folder1%2Ffolder2%2Ffolder3%2Fjdl-2/detail/experiment%2Fbuild-locally-docker/21/pipeline');
            });
        });
    });

    describe('badName003', () => {
        // it('should build the baseUrl if tabName omitted', () => {
        //     const url = badName003(
        //         'jenkins',
        //         'blueocean',
        //         'master',
        //         1,
        //     );
        //
        //     assert.equal(url, '/organizations/jenkins/blueocean/detail/master/1');
        // });
        // TODO: Is there anywhere we're actually relying on this ^^^ (ie, will it fail ATH)?

        it('should build the full url with tab name', () => {
            const url = badName003('jenkins', 'blueocean', 'master', 1, 'changes');

            assert.equal(url, '/organizations/jenkins/blueocean/detail/master/1/changes');
        });

        it('should escape characters correctly', () => {
            const url = badName003('jenkins', 'blueocean', 'feature/JENKINS-666', 1, 'changes');

            assert.equal(url, '/organizations/jenkins/blueocean/detail/feature%2FJENKINS-666/1/changes');
        });
    });
});

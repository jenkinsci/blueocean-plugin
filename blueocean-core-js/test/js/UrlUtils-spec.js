import { assert } from 'chai';

import { toClassicJobPage } from '../../src/js/utils/UrlUtils';

describe('UrlUtils', () => {
    it('toClassicJobPage - Non Multibranch', () => {
        assert.equal(toClassicJobPage(
            '/jenkins/blue/organizations/jenkins/freestyleA/detail/freestyleA/activity', false),
            '/job/freestyleA');
        assert.equal(toClassicJobPage(
            '/jenkins/blue/organizations/jenkins/freestyleA/detail/freestyleA/2/pipeline', false),
            '/job/freestyleA/2');

        // In a folder
        assert.equal(toClassicJobPage(
            '/jenkins/blue/organizations/jenkins/Foo%2FBar/activity', false),
            '/job/Foo/job/Bar');
        assert.equal(toClassicJobPage(
            '/jenkins/blue/organizations/jenkins/Foo%2FBar/detail/Bar/1/pipeline', false),
            '/job/Foo/job/Bar/1');
    });

    it('toClassicJobPage - Multibranch', () => {
        // A job down in a folder
        assert.equal(toClassicJobPage(
            '/jenkins/blue/organizations/jenkins/folder1%2Ffolder2%2FATH/activity', true),
            '/job/folder1/job/folder2/job/ATH');
        assert.equal(toClassicJobPage(
            '/jenkins/blue/organizations/jenkins/folder1%2Ffolder2%2FATH/detail/master/1/pipeline', true),
            '/job/folder1/job/folder2/job/ATH/job/master/1');
    });
});

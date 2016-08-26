/**
 * Created by cmeyers on 8/25/16.
 */
import { assert } from 'chai';

import { buildRunDetailsUrl } from '../../src/js/UrlBuilder';

const createObjectFromLink = (url) => {
    return { _links: { self: { href: url } } };
};

describe("buildRunDetailsUrl", () => {

    it("handles freestyle job", () => {
        const freestyle = createObjectFromLink(
            '/blue/rest/organizations/jenkins/pipelines/freestyle-jobs/pipelines/freestyle-success-10m/runs/28/'
        );

        const url = buildRunDetailsUrl(freestyle);
        assert.equal(url, '/organizations/jenkins/freestyle-jobs%2Ffreestyle-success-10m/detail/freestyle-success-10m/28/pipeline');
    });

    it("handles multi-branch job", () => {
        const multibranch = createObjectFromLink(
            '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/jdl-2/branches/experiment%252Fbuild-locally-docker/runs/21/'
        );

        const url = buildRunDetailsUrl(multibranch);
        assert.equal(url, '/organizations/jenkins/folder1%2Ffolder2%2Ffolder3%2Fjdl-2/detail/experiment%2Fbuild-locally-docker/21/pipeline');
    });

});

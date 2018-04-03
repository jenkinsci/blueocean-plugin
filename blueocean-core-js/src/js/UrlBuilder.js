/**
 * Created by cmeyers on 8/25/16.
 */

const extractRestUrl = subject => {
    let restUrl = null;

    if (typeof subject === 'object') {
        if (subject && subject._links && subject._links.self) {
            restUrl = subject._links.self.href;
        }
    } else if (typeof subject === 'string') {
        restUrl = subject;
    }

    if (!restUrl) {
        throw new Error('Could not find input URL');
    }

    return restUrl;
};

/**
 * Return a new array with leading and trailing whitespace elements removed.
 *
 * @param {Array} tokens
 * @returns {Array}
 */
const trimEmptyTokens = tokens => {
    const copy = tokens.slice();

    if (copy[0] === '') {
        copy.shift();
    }

    if (copy[copy.length - 1] === '') {
        copy.pop();
    }

    return copy;
};

// TODO: TS

// TODO: Docs or remove
function parseRestRunUrl(restUrl) {
    // TODO: If keeping this, replace it with a regexp or a more sensible token handler, once we have tests that take into account the variable tokens length

    const tokens = trimEmptyTokens(restUrl.split('/'));

    // given the following URL '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/jdl-2
    // /branches/experiment%252Fbuild-locally-docker/runs/21/

    const organizationName = decodeURIComponent(tokens[3]);
    const isMultiBranch = tokens[tokens.length - 4] === 'branches';

    const fullNameStart = 4;
    const fullNameEnd = isMultiBranch ? tokens.length - 4 : tokens.length - 2;
    // grab the tokens that make up the full name, then filter out the even values ('/pipelines')
    // so the clean folder path is returned, e.g. f1/pipelines/f2/pipelines/f3/pipelines/foo => f1/f2/f3/foo
    const fullName = tokens
        .slice(fullNameStart, fullNameEnd)
        .filter((name, index) => index % 2 === 1)
        .join('/');

    const pipelineName = tokens[fullNameEnd - 1];
    const branchName = isMultiBranch ? tokens[tokens.length - 3] : '';
    const runId = tokens[tokens.length - 1];

    const detailName = decodeURIComponent(isMultiBranch ? decodeURIComponent(branchName) : pipelineName);
    // TODO: Look into why branch names are double-encoded, if I dare

    // fail fast
    if (!organizationName || !fullName || !detailName || !runId) {
        throw new Error('Could not extract URI components');
    }

    // TODO: Create a type for this once in TS
    return {
        organizationName,
        fullName,
        detailName,
        runId,
    };
}

// TODO: Docs - Run object to run details url
export function badName001(runDetails) {
    const restUrl = runDetails._links.self.href;

    const { organizationName, fullName, detailName, runId } = parseRestRunUrl(restUrl);

    return badName003(organizationName, fullName, detailName, runId);
}

// TODO: Docs - Run link to run details url
export function badName002(restUrl) {
    const { organizationName, fullName, detailName, runId } = parseRestRunUrl(restUrl);

    return badName003(organizationName, fullName, detailName, runId);
}

// TODO: Rename, docs, check param names
// TODO: Add some unit tests, ensuring the individual components are URLEncoded correctly
function badName003(organizationName, pipelineFullName, branchOrPipelineName, runId, tabName = 'pipeline') {
    //   return `/organizations/${organizationName}` + `/${encodeURIComponent(fullName)}/detail` + `/${detailName}/${runId}/pipeline`;

    return (
        `/organizations/${encodeURIComponent(organizationName)}` +
        `/${encodeURIComponent(pipelineFullName)}` +
        `/detail/${encodeURIComponent(branchOrPipelineName)}` +
        `/${encodeURIComponent(runId)}` +
        `/${tabName}`
    );
}

// /**
//  * Builds the proper URL to view Run Details for the specified run.
//  * Run is either a run object with "_links.self.href" property, or the URL itself.
//  *
//  * @param {object|string} run
//  * @returns {string}
//  *
//  * FIXME-JM: Unify this with buildRunDetailsUrl in blueocean-core-js/src/js/utils/UrlUtils.js - this is madness.
//  */
// export const buildRunDetailsUrl = run => {
//
//     // TODO: Remove this
//     throw new Error("should not still be calling this");
//
//     const restUrl = extractRestUrl(run);
//
//     const {
//         organizationName,
//         fullName,
//         detailName,
//         runId
//     } = parseRestRunUrl(restUrl);
//
//     return `/organizations/${organizationName}` + `/${encodeURIComponent(fullName)}/detail` + `/${detailName}/${runId}/pipeline`;
// };
//
// export default {
//     buildRunDetailsUrl,
// };

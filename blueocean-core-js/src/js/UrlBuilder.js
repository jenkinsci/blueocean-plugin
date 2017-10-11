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

/**
 * Builds the proper URL to view Run Details for the specified run.
 * Run is either a run object with "_links.self.href" property, or the URL itself.
 *
 * @param {object|string} run
 * @returns {string}
 */
export const buildRunDetailsUrl = run => {
    const restUrl = extractRestUrl(run);
    const tokens = trimEmptyTokens(restUrl.split('/'));

    // given the following URL '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/jdl-2
    // /branches/experiment%252Fbuild-locally-docker/runs/21/

    const organizationName = tokens[3];
    const isMultiBranch = tokens[tokens.length - 4] === 'branches';

    const fullNameStart = 4;
    const fullNameEnd = !isMultiBranch ? tokens.length - 2 : tokens.length - 4;
    // grab the tokens that make up the full name, then filter out the even values ('/pipelines')
    // so the clean folder path is returned, e.g. folder1/folder2/folder3/jdl-2
    const fullName = tokens
        .slice(fullNameStart, fullNameEnd)
        .filter((name, index) => index % 2 === 1)
        .join('/');

    const pipelineName = tokens[fullNameEnd - 1];
    const branchName = isMultiBranch ? tokens[tokens.length - 3] : '';
    const runId = tokens[tokens.length - 1];

    const detailName = isMultiBranch ? decodeURIComponent(branchName) : pipelineName;

    // fail fast
    if (!organizationName || !fullName || !detailName || !runId) {
        throw new Error('Could not extract URI components');
    }

    return `/organizations/${organizationName}` + `/${encodeURIComponent(fullName)}/detail` + `/${detailName}/${runId}/pipeline`;
};

export default {
    buildRunDetailsUrl,
};

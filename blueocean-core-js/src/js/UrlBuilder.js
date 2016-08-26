/**
 * Created by cmeyers on 8/25/16.
 */

/**
 * Builds the proper URL to view Run Details for the specified run.
 * Run is either a run object with "_links.self.href" property, or the URL itself.
 *
 * @param {object|string} run
 * @returns {string}
 */
export const buildRunDetailsUrl = (run) => {
    let restUrl = null;

    if (typeof run === 'object') {
        if (run && run._links && run._links.self) {
            restUrl = run._links.self.href;
        }
    } else if (typeof run === 'string') {
        restUrl = run;
    }

    if (!restUrl) {
        throw new Error('Could not find input URL');
    }

    const tokens = restUrl.split('/');

    // given the following URL '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/jdl-2
    // /branches/experiment%252Fbuild-locally-docker/runs/21/

    // clean empty chars that result from URL beginning or ending with forward slash
    if (tokens[0] === '') {
        tokens.shift();
    }

    if (tokens[tokens.length - 1] === '') {
        tokens.pop();
    }

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

    return `/organizations/${organizationName}` +
        `/${encodeURIComponent(fullName)}/detail` +
        `/${detailName}/${runId}/pipeline`;
};

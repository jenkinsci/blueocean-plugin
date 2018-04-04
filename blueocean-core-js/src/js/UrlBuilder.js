// TODO: File docs

import AppConfig from './config';
import { classicOrganizationRoot } from './utils/UrlUtils';

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

// TODO: Docs - Run object -> run details url
export function badName001(runDetails) {
    const restUrl = runDetails._links.self.href;
    return badName002(restUrl);
}

// TODO: Docs - Run link -> run details url
export function badName002(restUrl) {
    const { organizationName, fullName, detailName, runId } = parseRestRunUrl(restUrl);

    return badName003(organizationName, fullName, detailName, runId);
}

// TODO: Docs - individual run detail params -> run details url
export function badName003(organizationName, pipelineFullName, branchOrPipelineName, runId, tabName = 'pipeline') {
    const baseUrl =
        `/organizations/${encodeURIComponent(organizationName)}` +
        `/${encodeURIComponent(pipelineFullName)}` +
        `/detail/${encodeURIComponent(branchOrPipelineName)}` +
        `/${encodeURIComponent(runId)}`;

    return tabName === null ? baseUrl : baseUrl + `/${tabName}`;
}

/**
 * Build a root-relative URL to the organization's pipeline list screen.
 * @param organization
 */
export function buildOrganizationUrl(organization) {
    return `/organizations/${encodeURIComponent(organization)}`;
}

/**
 * Build a root-relative URL to the pipeline details screen.
 * @param organizationName
 * @param pipelineFullName
 * @param tabName
 * @returns {string}
 */
export function buildPipelineUrl(organizationName, pipelineFullName, tabName) {
    const baseUrl = `/organizations/${encodeURIComponent(organizationName)}/` + `${encodeURIComponent(pipelineFullName)}`;
    return tabName ? `${baseUrl}/${tabName}` : baseUrl;
}

export function classicJobRoot(pipelineFullName) {
    // TODO: check usages, adjust name to show classic-relatedness and param
    const jenkinsUrl = AppConfig.getJenkinsRootURL();
    return `${jenkinsUrl}${classicOrganizationRoot(AppConfig.getOrganizationGroup())}/job/${pipelineFullName.split('/').join('/job/')}`;
}

export function buildClassicCreateJobUrl() {
    const jenkinsUrl = AppConfig.getJenkinsRootURL();
    return `${jenkinsUrl}${classicOrganizationRoot(AppConfig.getOrganizationGroup())}/newJob`;
}

export function buildClassicConfigUrl(pipelineDetails) {
    if (pipelineDetails && pipelineDetails.fullName) {
        return `${classicJobRoot(pipelineDetails.fullName)}/configure`;
    }
    return null;
}

export function buildClassicInputUrl(pipelineDetails, branchName, runId) {
    if (pipelineDetails && pipelineDetails.fullName) {
        if (pipelineDetails.branchNames) {
            return `${classicJobRoot(pipelineDetails.fullName)}/job/${encodeURIComponent(branchName)}/${encodeURIComponent(runId)}/input`;
        } else {
            return `${classicJobRoot(pipelineDetails.fullName)}/${encodeURIComponent(runId)}/input`;
        }
    }
    return null;
}

// http://localhost:8080/jenkins/job/scherler/job/Jenkins-40617-params/build?delay=0sec
export function buildClassicBuildUrl(pipelineDetails) {
    if (pipelineDetails && pipelineDetails.fullName) {
        return `${classicJobRoot(pipelineDetails.fullName)}/build?delay=0sec`;
    }
    return null;
}

/**
 * Check is the current Blue ocean page a pipeline page and if so,
 * decode it to the corresponding classic Jenkins Job page.
 * @returns {string|undefined} The classic job page, or undefined if
 * it was unable to decode the page URL.
 */
export function toClassicJobPage(currentPageUrl, isMultibranch = false) {
    const pageUrlTokens = currentPageUrl.split('/').filter(token => typeof token === 'string' && token !== '');

    // Remove all path elements up to and including the Jenkins
    // organization name.
    let token = pageUrlTokens.shift();
    while (token !== undefined && token !== 'organizations') {
        token = pageUrlTokens.shift();
    }

    let classicJobFullName = classicOrganizationRoot(AppConfig.getOrganizationGroup());

    if (pageUrlTokens.length > 1) {
        // The next token is the actual organization name e.g. "jenkins".
        // Remove that since we don't need it.
        pageUrlTokens.shift();

        // The next token is the "full" job name, URL encoded.
        const fullJobName = decodeURIComponent(pageUrlTokens.shift());
        const fullJobNameTokens = fullJobName.split('/');
        if (fullJobName !== 'pipelines' && pageUrlTokens.length > 0) {
            classicJobFullName = classicJobFullName + '/job/' + fullJobNameTokens.join('/job/');
        }
        if (pageUrlTokens.length > 1) {
            // The next token being "detail" indicates that we're looking
            // at a branch.
            if (pageUrlTokens.shift() === 'detail') {
                // is going to be something like one of:
                // - detail/[freestyleA/activity]
                // - detail/[freestyleA/2/pipeline]
                if (isMultibranch) {
                    const branchName = pageUrlTokens.shift(); // "freestyleA"
                    const classicJobBranch = classicJobFullName + '/job/' + branchName;

                    // And if there's more than one token left then we have
                    // the detail/freestyleA/[2/pipeline] variant. The next
                    // token is the runId
                    if (pageUrlTokens.length > 1) {
                        return classicJobBranch + '/' + pageUrlTokens.shift(); // "2"
                    }

                    return classicJobBranch;
                } else if (pageUrlTokens.length > 2) {
                    // And if there's more than two tokens left then we have
                    // the detail/[freestyleA/2/pipeline] variant.
                    // Next token is the branch name - not really a branch name !!
                    // Ignoring it.
                    pageUrlTokens.shift(); // "freestyleA"
                    // And the next token is the runId.
                    const runId = pageUrlTokens.shift(); // "2"
                    return classicJobFullName + '/' + runId;
                }
            }
        }
    }

    return classicJobFullName;
}

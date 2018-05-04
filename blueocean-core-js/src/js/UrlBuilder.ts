/*********************************************************************************************
 **********************************************************************************************

 Builders to specific screens, REST resources, and classic functionality

 For general URL-wrangling helpers please see the UrlUtils module.

 **********************************************************************************************
 *********************************************************************************************/

import { AppConfig } from './config';
import { UrlUtils } from './utils/UrlUtils';

/**
 * Return a new array with leading and trailing whitespace elements removed.
 */
function trimEmptyTokens(tokens) {
    const copy = tokens.slice();

    if (copy[0] === '') {
        copy.shift();
    }

    if (copy[copy.length - 1] === '') {
        copy.pop();
    }

    return copy;
}

type ResourceIdentifiers = {
    organizationName: string;
    pipelineFullName: string; // Includes folder path
    detailName: string; // Either the branchName or the pipeline short name if not multibranch
    runId: string;
};

/**
 * Parses the REST link to a pipeline run, extracts and unencodes the separate parts
 *
 * @param restUrl
 * @returns {{organizationName: string; fullName: string; detailName: string; runId: string}}
 */
function parseRestRunUrl(restUrl: string): ResourceIdentifiers {
    // FIXME: before exporting, make this more flexible so it can be a complete compliment to buildRestUrl

    const tokens = trimEmptyTokens(restUrl.split('/'));

    // given the following URL '/blue/rest/organizations/jenkins/pipelines/folder1/pipelines/folder2/pipelines/folder3/pipelines/jdl-2
    // /branches/experiment%252Fbuild-locally-docker/runs/21/

    const organizationName = decodeURIComponent(tokens[3]);
    const isMultiBranch = tokens[tokens.length - 4] === 'branches';

    const fullNameStart = 4;
    const fullNameEnd = isMultiBranch ? tokens.length - 4 : tokens.length - 2;
    // grab the tokens that make up the full name, then filter out the even values ('/pipelines')
    // so the clean folder path is returned, e.g. f1/pipelines/f2/pipelines/f3/pipelines/foo => f1/f2/f3/foo
    const pipelineFullName = tokens
        .slice(fullNameStart, fullNameEnd)
        .filter((name, index) => index % 2 === 1)
        .join('/');

    const pipelineName = tokens[fullNameEnd - 1];
    const branchName = isMultiBranch ? tokens[tokens.length - 3] : '';
    const runId = tokens[tokens.length - 1];

    const detailName = decodeURIComponent(isMultiBranch ? decodeURIComponent(branchName) : pipelineName);

    // fail fast
    if (!organizationName || !pipelineFullName || !detailName || !runId) {
        throw new Error('Could not extract URI components');
    }

    return {
        organizationName,
        pipelineFullName,
        detailName,
        runId,
    };
}

export type RunDetailsWithSelfLink = {
    // FIXME: We need a canonical typedef for run details object
    _links: { self: { href: string } };
};

/**
 * Builds a run details view url from a RunDetails object
 */
export function buildRunUrlForDetails(runDetails: RunDetailsWithSelfLink) {
    const restUrl = runDetails._links.self.href;
    return buildRunUrlForRestUrl(restUrl);
}

/**
 * Builds a run details view url from a run's REST link URL
 */
export function buildRunUrlForRestUrl(restUrl: string) {
    const { organizationName, pipelineFullName, detailName, runId } = parseRestRunUrl(restUrl);

    return buildRunUrl(organizationName, pipelineFullName, detailName, runId);
}

/**
 * Builds a run details view url from identifiers
 */
export function buildRunUrl(organizationName, pipelineFullName, branchOrPipelineName, runId, tabName = 'pipeline') {
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

/**
 * Gives classic jenkins job path prefix.
 * For organization group '/folder1/org1', job prefix is: /job/folder1/job/org1
 * For root organization group '/', there is no prefix: ''.
 * @param organizationGroupName organization group
 * @returns {string}
 */
export function classicOrganizationRoot(organizationGroupName) {
    if (organizationGroupName && organizationGroupName !== '/') {
        return `${organizationGroupName.split('/').join('/job/')}`;
    }
    return '';
}

/**
 * The root of a classic URL for a specific pipeline using the default org group
 * @param pipelineFullName
 * @returns {string}
 */
export function classicJobRoot(pipelineFullName) {
    const jenkinsUrl = AppConfig.getJenkinsRootURL();
    const orgRoot = classicOrganizationRoot(AppConfig.getOrganizationGroup());
    const classicFolderPath = pipelineFullName.split('/').join('/job/');
    return `${jenkinsUrl}${orgRoot}/job/${classicFolderPath}`;
}

/**
 * URL to "create job" page in classic
 */
export function buildClassicCreateJobUrl() {
    const jenkinsUrl = AppConfig.getJenkinsRootURL();
    return `${jenkinsUrl}${classicOrganizationRoot(AppConfig.getOrganizationGroup())}/newJob`;
}

/**
 * URL to the pipeline configuration screen in classic
 */
export function buildClassicConfigUrl(pipelineDetails) {
    if (pipelineDetails && pipelineDetails.fullName) {
        return `${classicJobRoot(pipelineDetails.fullName)}/configure`;
    }
    return null;
}

/**
 * URL to the classic input sceen for a specific run
 */
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

/**
 * URL to classic to trigger a build
 *
 * example: http://localhost:8080/jenkins/job/scherler/job/Jenkins-40617-params/build?delay=0sec
 */
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
        let fullJobName = decodeURIComponent(pageUrlTokens.shift());

        // If the URL comes from pipeline-editor then the "full" job name is the next token
        if (fullJobName === 'pipeline-editor' && pageUrlTokens.length === 2) {
            fullJobName = decodeURIComponent(pageUrlTokens.shift());
        }

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

/**
 * Build a REST resource URL from component identifiers.
 *
 * @param organizationName the name of the owning organisation, including org folders
 * @param pipelineFullName (optional) the full name of a pipeline, including folders
 * @param branchName (optional) branch name for multibranch projects
 * @param runId (optional) identifies an individual run
 * @returns a URL string
 */
export function buildRestUrl(organizationName: string, pipelineFullName?: string, branchName?: string, runId?: string) {
    const jenkinsUrl = AppConfig.getJenkinsRootURL();
    let url = `${jenkinsUrl}/blue/rest/organizations/${encodeURIComponent(organizationName)}`;

    if (pipelineFullName) {
        // pipelineFullName might include a folder path, and final component is already encoded
        url += `/pipelines/${pipelineFullName}`;
    }

    if (branchName) {
        // JENKINS-37712 branch needs to be double-encoded for some reason
        url += `/branches/${UrlUtils.doubleUriEncode(branchName)}`;
    }

    if (runId) {
        url += `/runs/${encodeURIComponent(runId)}`;
    }

    return UrlUtils.ensureTrailingSlash(url);
}

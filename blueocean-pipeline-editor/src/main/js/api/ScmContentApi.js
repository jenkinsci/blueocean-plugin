import { Fetch, getRestUrl } from '@jenkins-cd/blueocean-core-js';
import TypedError from './TypedError';


const Base64 = {
    encode: (data) => btoa(data),
    decode: (str) => atob(str),
};

export const LoadError = {
    TOKEN_NOT_FOUND: 'TOKEN_NOT_FOUND',
    TOKEN_REVOKED: 'TOKEN_REVOKED',
    TOKEN_INVALID_SCOPES: 'TOKEN_INVALID_SCOPES',
    JENKINSFILE_NOT_FOUND: 'JENKINSFILE_NOT_FOUND',
};


/**
 * Load and save content to a pipeline's backing SCM provider.
 */
class ScmContentApi {

    loadContent({organization, pipeline, branch, path = 'Jenkinsfile'}) {
        let contentUrl = `${getRestUrl({ organization, pipeline })}scm/content?path=${path}`;
        if (branch) {
            contentUrl += `&branch=${encodeURIComponent(branch)}`;
        }
        return Fetch.fetchJSON(contentUrl)
            .then(result => result)
            .catch(error => this._loadContentErrorHandler(error));
    }

    _loadContentErrorHandler(error) {
        const { status } = error.response;
        const { responseBody } = error;
        const { message } = responseBody || {};

        if (status === 404) {
            throw new TypedError(LoadError.JENKINSFILE_NOT_FOUND, responseBody);
        } else if (status === 428) {
            throw new TypedError(LoadError.TOKEN_NOT_FOUND, responseBody);
        } else if (message.indexOf('Invalid accessToken') !== -1) {
            throw new TypedError(LoadError.TOKEN_REVOKED, responseBody);
        }

        // TODO: TOKEN_INVALID_SCOPES

        throw error;
    }

    buildSaveContentRequest({organization, pipeline, repo, sourceBranch, targetBranch, sha, message, path = 'Jenkinsfile', content}) {
        return {
            content: {
                message,
                path,
                branch: targetBranch,
                sourceBranch,
                repo,
                sha,
                base64Data: Base64.encode(content),
            }
        };
    }

    saveContent({organization, pipeline, repo, sourceBranch, targetBranch, sha, message, path = 'Jenkinsfile', content}) {
        const contentUrl = `${getRestUrl({organization, pipeline})}scm/content/`;

        const body = this.buildSaveContentRequest({
            organization, pipeline, repo, sourceBranch, targetBranch, sha, message, path, content,
        });

        const fetchOptions = {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
        };

        return Fetch.fetchJSON(contentUrl, { fetchOptions });
    }

}


export default ScmContentApi;

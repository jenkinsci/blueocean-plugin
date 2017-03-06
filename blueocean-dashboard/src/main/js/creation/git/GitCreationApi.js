import es6Promise from 'es6-promise'; es6Promise.polyfill();
import { capabilityAugmenter, Fetch, UrlConfig, Utils } from '@jenkins-cd/blueocean-core-js';

import { Enum } from '../flow2/Enum';


const CODE_VALIDATION_FAILED = 400;

const ERROR_FIELD_CODE_CONFLICT = 'ALREADY_EXISTS';

const ERROR_FIELD_SCM_URI = 'scmConfig.uri';
// received with completely bogus URI
const ERROR_MESSAGE_INVALID_URI = 'org.eclipse.jgit.api.errors.TransportException';
// received when pointing at non-existent repo, or repo with insufficient creds
const ERROR_MESSAGE_INVALID_CREDENTIAL = 'Cannot open session, connection is not authenticated.';
// TODO: what kind of UI error do we transform this to?
const ERROR_MESSAGE_WHAT_DOES_THIS_MEAN = 'Authentication is required but no CredentialsProvider has been registered';

export const CreatePipelineOutcome = new Enum({
    SUCCESS: 'success',
    INVALID_URI: 'invalid_uri',
    INVALID_CREDENTIAL: 'invalid_credential',
    INVALID_NAME: 'invalid_name',
    ERROR: 'error',
});


function hasErrorFieldCode(errors, code) {
    return errors
            .filter(err => err.code === code)
            .length > 0;
}

function hasErrorFieldName(errors, fieldName) {
    return errors
            .filter(err => err.field === fieldName)
            .length > 0;
}


/**
 * Proxy to the backend REST API.
 */
export default class GitCreationApi {

    constructor(fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
    }

    createPipeline(repositoryUrl, credentialId, name) {
        const path = UrlConfig.getRestBaseURL();
        const createUrl = Utils.cleanSlashes(`${path}/organizations/jenkins/pipelines`);

        const requestBody = {
            name,
            $class: 'io.jenkins.blueocean.blueocean_git_pipeline.GitPipelineCreateRequest',
            scmConfig: {
                uri: repositoryUrl,
                credentialId,
            },
        };

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(createUrl, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(
                pipeline => this._createPipelineSuccess(pipeline),
                error => this._createPipelineFailure(error),
            );
    }

    _createPipelineSuccess(pipeline) {
        return {
            outcome: CreatePipelineOutcome.SUCCESS,
            pipeline,
        };
    }

    _createPipelineFailure(error) {
        const { code, errors } = error.responseBody;

        if (code === CODE_VALIDATION_FAILED) {
            if (errors.length === 1 && hasErrorFieldCode(errors, ERROR_FIELD_CODE_CONFLICT)) {
                return {
                    outcome: CreatePipelineOutcome.INVALID_NAME,
                };
            }

            // TODO: handle other error types

            /*
            if (this._hasError(errors, ERROR_FIELD_SCM_URI)) {
                return {
                    outcome: CreatePipelineOutcome.INVALID_URI,
                };
            }
            */
        }

        return {
            outcome: CreatePipelineOutcome.ERROR,
            error: error.responseBody,
        };
    }

    checkPipelineNameAvailable(name) {
        const path = UrlConfig.getRestBaseURL();
        const checkUrl = Utils.cleanSlashes(`${path}/organizations/jenkins/pipelines/${name}`);

        const fetchOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return this._fetch(checkUrl, { fetchOptions })
            .then(() => false, () => true);
    }

}

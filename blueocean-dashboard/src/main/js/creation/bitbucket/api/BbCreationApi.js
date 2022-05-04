import { capabilityAugmenter, Fetch, UrlConfig, Utils, AppConfig } from '@jenkins-cd/blueocean-core-js';
import { Enum } from '../../flow2/Enum';

const ERROR_FIELD_SCM_CREDENTIAL = 'credentialId';
const CODE_VALIDATION_FAILED = 400;

const ERROR_FIELD_CODE_CONFLICT = 'ALREADY_EXISTS';

const ERROR_FIELD_SCM_URI = 'scmConfig.uri';

export const ListOrganizationsOutcome = new Enum({
    SUCCESS: 'success',
    INVALID_CREDENTIAL_ID: 'invalid_credential_id',
    ERROR: 'error',
});

export const CreateMbpOutcome = new Enum({
    SUCCESS: 'success',
    INVALID_URI: 'invalid_uri',
    INVALID_CREDENTIAL: 'invalid_credential',
    INVALID_NAME: 'invalid_name',
    ERROR: 'error',
});

function hasErrorFieldName(errors, fieldName) {
    return errors.filter(err => err.field === fieldName).length > 0;
}

function hasErrorFieldCode(errors, code) {
    return errors.filter(err => err.code === code).length > 0;
}

export class BbCreationApi {
    constructor(scmId, fetch) {
        this._fetch = fetch || Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId;
        this.partialLoadedOrganizations = [];
    }

    listOrganizations(credentialId, apiUrl, pagedOrgsStart = 0, pageSize = 100) {
        const path = UrlConfig.getJenkinsRootURL();
        const orgsUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/${this.organization}/scm/${
                this.scmId
            }/organizations/?credentialId=${credentialId}&start=${pagedOrgsStart}&limit=100&apiUrl=${apiUrl}`,
            false
        );

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        };

        return this._fetch(orgsUrl, { fetchOptions })
            .then(orgs => capabilityAugmenter.augmentCapabilities(orgs))
            .then(orgs => this._listOrganizationsSuccess(orgs, credentialId, apiUrl, pagedOrgsStart), error => this._listOrganizationsFailure(error));
    }

    _listOrganizationsSuccess(organizations, credentialId, apiUrl, pagedOrgsStart) {
        this.partialLoadedOrganizations = this.partialLoadedOrganizations.concat(organizations);

        if (organizations.length >= 100) {
            //if we got 100 or more orgs, we need to check the next page to see if there are any more orgs
            return this.listOrganizations(credentialId, apiUrl, pagedOrgsStart + 100);
        } else {
            return {
                outcome: 'SUCCESS',
                organizations: this.partialLoadedOrganizations,
            };
        }
    }

    _listOrganizationsFailure(error) {
        const { code, message, errors } = error.responseBody;

        if (code === 400) {
            if (hasErrorFieldName(errors, ERROR_FIELD_SCM_CREDENTIAL)) {
                return {
                    outcome: ListOrganizationsOutcome.INVALID_CREDENTIAL_ID,
                };
            }
        }
        return {
            outcome: ListOrganizationsOutcome.ERROR,
            error: message,
        };
    }

    listRepositories(credentialId, apiUrl, organizationName, pageNumber = 1, pageSize = 100) {
        const path = UrlConfig.getJenkinsRootURL();
        const reposUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/${this.organization}/scm/${this.scmId}/organizations/${organizationName}/repositories/` +
                `?credentialId=${credentialId}&pageNumber=${pageNumber}&pageSize=${pageSize}&apiUrl=${apiUrl}`
        );
        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        };

        return this._fetch(reposUrl, { fetchOptions })
            .then(response => capabilityAugmenter.augmentCapabilities(response));
    }

    createMbp(credentialId, scmId, apiUrl, itemName, bbOrganizationKey, repoName, creatorClass) {
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/pipelines/`);

        const requestBody = this._buildRequestBody(credentialId, scmId, apiUrl, itemName, bbOrganizationKey, repoName, creatorClass);

        const fetchOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody),
        };

        return this._fetch(createUrl, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(pipeline => this._createMbpSuccess(pipeline), error => this._createMbpFailure(error));
    }

    checkPipelineNameAvailable(name) {
        const path = UrlConfig.getRestBaseURL();
        const checkUrl = Utils.cleanSlashes(`${path}/organizations/${this.organization}/pipelines/${name}`);

        const fetchOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return this._fetch(checkUrl, { fetchOptions }).then(() => false, () => true);
    }

    _createMbpSuccess(pipeline) {
        return {
            outcome: CreateMbpOutcome.SUCCESS,
            pipeline,
        };
    }

    _createMbpFailure(error) {
        const { code, errors } = error.responseBody;

        if (code === CODE_VALIDATION_FAILED) {
            if (errors.length === 1 && hasErrorFieldCode(errors, ERROR_FIELD_CODE_CONFLICT)) {
                return {
                    outcome: CreateMbpOutcome.INVALID_NAME,
                };
            }

            if (hasErrorFieldName(errors, ERROR_FIELD_SCM_URI)) {
                return {
                    outcome: CreateMbpOutcome.INVALID_URI,
                };
            } else if (hasErrorFieldName(errors, ERROR_FIELD_SCM_CREDENTIAL)) {
                return {
                    outcome: CreateMbpOutcome.INVALID_CREDENTIAL,
                };
            }
        }

        return {
            outcome: CreateMbpOutcome.ERROR,
            error: error.responseBody,
        };
    }

    _buildRequestBody(credentialId, scmId, apiUrl, itemName, organizationName, repoName, creatorClass) {
        return {
            name: itemName,
            $class: creatorClass,
            scmConfig: {
                id: scmId,
                credentialId,
                uri: apiUrl,
                config: {
                    repoOwner: organizationName,
                    repository: repoName,
                },
            },
        };
    }

    findBranches(pipelineName) {
        const path = UrlConfig.getJenkinsRootURL();
        const pipelineUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/${this.organization}/pipelines/${pipelineName}/`);
        return this._fetch(pipelineUrl)
            .then(response => capabilityAugmenter.augmentCapabilities(response))
            .then(pipeline => this._findBranchesSuccess(pipeline), error => this._findBranchesFailure(error));
    }

    _findBranchesSuccess(pipeline) {
        return {
            isFound: pipeline.getTotalNumberOfBranches > 0,
            hasError: false,
            pipeline,
        };
    }

    _findBranchesFailure(error) {
        return {
            hasError: true,
            error,
        };
    }
}

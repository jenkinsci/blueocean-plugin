import {Enum} from "../../flow2/Enum";
import {AppConfig, capabilityAugmenter, Fetch, UrlConfig, Utils} from "@jenkins-cd/blueocean-core-js";

const ERROR_FIELD_SCM_CREDENTIAL = 'credentialId';
const CODE_VALIDATION_FAILED = 400;

const ERROR_FIELD_CODE_CONFLICT = 'ALREADY_EXISTS';

const ERROR_FIELD_SCM_URI = 'scmConfig.uri';

export const ListProjectsOutcome = new Enum({
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

/**
 * Handles lookup of Perforce projects and repos.
 */
export default class PerforceCreationApi {
    constructor(scmId) {
        this._fetch = Fetch.fetchJSON;
        this.organization = AppConfig.getOrganizationName();
        this.scmId = scmId;
        this.partialLoadedProjects = [];
        console.log("PerforceCreationApi: constructor: scmId: " + this.scmId);
    }

    listProjects(credentialId, pagedProjStart = 0, pageSize = 100) {
        const path = UrlConfig.getJenkinsRootURL();
        const credUrl = Utils.cleanSlashes(`${path}/swarm/projects/?credential=` + credentialId);
        console.log("Project url: " + credUrl);
        return this._fetch(credUrl)
            .then(projects => capabilityAugmenter.augmentCapabilities(projects))
            .then(projects => this._listProjectsSuccess(projects, credentialId, pagedProjStart), error => this._listProjectsFailure(error));
    }

    _listProjectsSuccess(projects, credentialId, pagedOrgsStart) {
        //TODO Later: Implement pagination
        this.partialLoadedProjects = this.partialLoadedProjects.concat(projects);
        return {
            outcome: 'SUCCESS',
            projects: this.partialLoadedProjects,
        };
    }

    _listProjectsFailure(error) {
        console.log("PerforceCreationApi._listProjectFailure().error: " + error);

        return {
            outcome: ListProjectsOutcome.ERROR,
            error: error,
        };
    }

    createMbp(credentialId, projectName, pipelineName) {
        const path = UrlConfig.getJenkinsRootURL();
        const createUrl = Utils.cleanSlashes(`${path}/swarm/create/?credential=${credentialId}&project=${projectName}&name=${pipelineName}`);

        console.log("createUrl: " + createUrl);
        const fetchOptions = {
            method: 'POST',
        };

        return this._fetch(createUrl, {fetchOptions})
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

        return this._fetch(checkUrl, {fetchOptions}).then(() => false, () => true);
    }

    _createMbpSuccess(pipeline) {
        console.log("PerforceCreationApi._createMbpSuccess.pipeline.pipelineFullName: " + pipeline.pipelineFullName);
        return {
            outcome: CreateMbpOutcome.SUCCESS,
            pipeline,
        };
    }

    _createMbpFailure(error) {
        console.log("PerforceCreationApi._createMbpFailure.error.responseBody: here:  " + error.responseBody);

        const {code, errors} = error.responseBody;

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

        //TODO Change this to handle better
        return {
            outcome: CreateMbpOutcome.INVALID_NAME,
            //error: error.responseBody,
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
            isFound: pipeline.getTotalNumberOfBranches > -1,
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

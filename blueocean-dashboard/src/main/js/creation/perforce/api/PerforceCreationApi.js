import {Enum} from "../../flow2/Enum";
import {AppConfig, capabilityAugmenter, Fetch, UrlConfig, Utils} from "@jenkins-cd/blueocean-core-js";

const ERROR_FIELD_SCM_CREDENTIAL = 'credentialId';

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

    listProjects(credentialId, apiUrl, pagedOrgsStart = 0, pageSize = 100) {
        const path = UrlConfig.getJenkinsRootURL();
        /*const orgsUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/${this.organization}/scm/${
                this.scmId
                }/organizations/?credentialId=${credentialId}&start=${pagedOrgsStart}&limit=100&apiUrl=${apiUrl}`,
            false
        );*/
        const credUrl = Utils.cleanSlashes(`${path}/credentials/store/system/domain/_/api/json?tree=credentials[id,typeName]`);
        //TODO Change this to get actual project list from Swarm

        return this._fetch(credUrl)
            .then(projects => capabilityAugmenter.augmentCapabilities(projects))
            .then(projects => this._listProjectsSuccess(projects, credentialId, apiUrl, pagedOrgsStart), error => this._listProjectsFailure(error));
    }

    _listProjectsSuccess(projects, credentialId, apiUrl, pagedOrgsStart) {
        //TODO Understand why this was done?
        //this.partialLoadedProjects = this.partialLoadedProjects.concat(projects);
        this.partialLoadedProjects = projects;
        console.log("PerforceCreationApi._listProjectsSuccess(): Projects: " + projects);

        if (projects.length >= 100) {
            //if we got 100 or more projects, we need to check the next page to see if there are any more projects
            return this.listProjects(credentialId, apiUrl, pagedOrgsStart + 100);
        } else {
            return {
                outcome: 'SUCCESS',
                projects: this.partialLoadedProjects,
            };
        }
    }

    _listProjectsFailure(error) {
        const {code, message, errors} = error.responseBody;

        if (code === 400) {
            if (hasErrorFieldName(errors, ERROR_FIELD_SCM_CREDENTIAL)) {
                return {
                    outcome: ListProjectsOutcome.INVALID_CREDENTIAL_ID,
                };
            }
        }
        return {
            outcome: ListProjectsOutcome.ERROR,
            error: message,
        };
    }

    createMbp(credentialId, scmId, apiUrl, itemName, bbOrganizationKey, repoName, creatorClass) {
        return this._fetch("http://localhost:4567/user/getUsers")
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(pipeline => this._createMbpSuccess(pipeline), error => this._createMbpFailure(error));
    }

    _createMbpSuccess(pipeline) {
        return {
            outcome: CreateMbpOutcome.SUCCESS,
            pipeline,
        };
    }

    _createMbpFailure(error) {
        //const { code, errors } = error.responseBody;

        /*   if (code === CODE_VALIDATION_FAILED) {
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
   */
        return {
            outcome: CreateMbpOutcome.ERROR,
            error: error.responseBody,
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

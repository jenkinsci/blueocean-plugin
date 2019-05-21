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

    listProjects(credentialId, pagedProjStart = 0, pageSize = 100) {
        const path = UrlConfig.getJenkinsRootURL();
        /*const orgsUrl = Utils.cleanSlashes(
            `${path}/blue/rest/organizations/${this.organization}/scm/${
                this.scmId
                }/organizations/?credentialId=${credentialId}&start=${pagedOrgsStart}&limit=100&apiUrl=${apiUrl}`,
            false
        );*/
        //TODO Remove hardcoding from the url.
        const credUrl = Utils.cleanSlashes(`${path}/swarm/projects/?credential=` + credentialId);
        console.log("Project url: " + credUrl);
        return this._fetch(credUrl)
            .then(projects => capabilityAugmenter.augmentCapabilities(projects))
            .then(projects => this._listProjectsSuccess(projects, credentialId, pagedProjStart), error => this._listProjectsFailure(error));
    }

    _listProjectsSuccess(projects, credentialId, pagedOrgsStart) {
        //TODO Later: Implement pagination
        this.partialLoadedProjects = this.partialLoadedProjects.concat(projects);
        //this.partialLoadedProjects = projects;
        // if (projects.length >= 100) {
        //     //if we got 100 or more projects, we need to check the next page to see if there are any more projects
        //     return this.listProjects(credentialId, pagedOrgsStart + 100);
        // } else {
            return {
                outcome: 'SUCCESS',
                projects: this.partialLoadedProjects,
            };
        //}
    }

    _listProjectsFailure(error) {
        console.log("PerforceCreationApi._listProjectFailure().error: " + error);
        /*const {code, message, errors} = error.responseBody;

        if (code === 400) {
            if (hasErrorFieldName(errors, ERROR_FIELD_SCM_CREDENTIAL)) {
                return {
                    outcome: ListProjectsOutcome.INVALID_CREDENTIAL_ID,
                };
            }
        }*/
        return {
            outcome: ListProjectsOutcome.ERROR,
            error: error,
        };
    }

    createMbp(credentialId, projectName, pipelineName) {
        /*
        Check if the pipeline name is available
        Yes
            Make a POST call to create pipeline. Return to createPipelineComplete with a good result
        No
            Return to createPipelineComplete() with result
         */

        const path = UrlConfig.getJenkinsRootURL();


        //http://localhost:9090/jenkins/swarm/create/?credential=p4prod&project=hip&name=hip3
        const createUrl = Utils.cleanSlashes(`${path}/swarm/create/?credential=${credentialId}&project=${projectName}&name=${pipelineName}`);
        const requestBody = this._buildRequestBody(credentialId, projectName, pipelineName);
        console.log("createUrl: " + createUrl);
        const fetchOptions = {
            method: 'POST',
        };
        //return true;

        return this._fetch(createUrl, { fetchOptions })
            .then(data => capabilityAugmenter.augmentCapabilities(data))
            .then(pipeline => this._createMbpSuccess(pipeline), error => this._createMbpFailure(error));
    }


    checkPipelineNameAvailable(name) {
        const path = UrlConfig.getRestBaseURL();
        //TODO Replace jenkins with ${this.organization}
        const checkUrl = Utils.cleanSlashes(`${path}/organizations/jenkins/pipelines/${name}`);

        const fetchOptions = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        };

        return this._fetch(checkUrl, { fetchOptions }).then(() => false, () => true);
    }

    _nameAvailableSuccess() {
        console.log("PerforceCreationApi returning success");
        return {
            outcome: CreateMbpOutcome.SUCCESS,
        };
    }

    _nameAvailableFailure() {
        console.log("PerforceCreationApi returning failure");
        return {
            outcome: CreateMbpOutcome.INVALID_NAME,
        };
    }


    _buildRequestBody(credentialId, projectName, pipelineName) {
        return {
            credential: credentialId,
            project: projectName,
            name: pipelineName,
        };
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
        //TODO Change this to handle better
        return {
            outcome: CreateMbpOutcome.INVALID_NAME,
            //error: error.responseBody,
        };
    }

    findBranches(pipelineName) {
        const path = UrlConfig.getJenkinsRootURL();
        //TODO jenkins
        const pipelineUrl = Utils.cleanSlashes(`${path}/blue/rest/organizations/jenkins/pipelines/${pipelineName}/`);
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

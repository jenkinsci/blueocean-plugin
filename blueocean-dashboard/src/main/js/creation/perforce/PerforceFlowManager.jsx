import React from 'react';
import {action, computed, observable} from 'mobx';

import {i18nTranslator, logging, sseService, pipelineService} from '@jenkins-cd/blueocean-core-js';

const translate = i18nTranslator('blueocean-dashboard');

import FlowManager from '../CreationFlowManager';
import waitAtLeast from '../flow2/waitAtLeast';
import STATE from "../perforce/PerforceCreationState";
import PerforceLoadingStep from "./steps/PerforceLoadingStep";
import PerforceCredentialsStep from "./steps/PerforceCredentialStep";
import PerforceCredentialsManager from "../../credentials/perforce/PerforceCredentialsManager";
import {ListProjectsOutcome} from './api/PerforceCreationApi';
import PerforceProjectListStep from './steps/PerforceProjectListStep';
import PerforceCompleteStep from './steps/PerforceCompleteStep';
import {CreateMbpOutcome} from "./api/PerforceCreationApi";
import PerforceRenameStep from "./steps/PerforceRenameStep";
import PerforceUnknownErrorStep from "./steps/PerforceUnknownErrorStep";


const LOGGER = logging.logger('io.jenkins.blueocean.p4-pipeline');
const MIN_DELAY = 500;

/**
 * Impl of FlowManager for perforce creation flow.
 */
export default class PerforceFlowManager extends FlowManager {

    selectedCred = null;
    pipelineName = null;
    pipeline = null;

    @observable projects = [];

    @observable selectedProject = null;

    @computed
    get stepsDisabled() {
        return (
            this.stateId === STATE.STEP_COMPLETE_EVENT_ERROR ||
            this.stateId === STATE.STEP_COMPLETE_EVENT_TIMEOUT ||
            this.stateId === STATE.STEP_COMPLETE_MISSING_JENKINSFILE ||
            this.stateId === STATE.PENDING_CREATION_SAVING ||
            this.stateId === STATE.PENDING_CREATION_EVENTS ||
            this.stateId === STATE.STEP_COMPLETE_SUCCESS
        );
    }

    constructor(creationApi, credentialApi) {
        super();
        this._creationApi = creationApi;
        this.credManager = new PerforceCredentialsManager(credentialApi);
    }

    translate(key, opts) {
        return translate(key, opts);
    }

    getScmId() {
        return 'perforce';
    }

    getState() {
        return STATE;
    }

    getStates() {
        return STATE.values();
    }

    getInitialStep() {
        return {
            stateId: STATE.PENDING_LOADING_CREDS,
            stepElement: <PerforceLoadingStep/>,
        };
    }

    onInitialized() {
        this._loadCredentialsList();
        this.setPlaceholders(translate('creation.core.status.completed'));
    }


    /*getApiUrl() {
        return this.selectedCred ? this.selectedCred.apiUrl : null;
    }*/

    _loadCredentialsList() {
        return this.credManager
            .findExistingCredential()
            .then(waitAtLeast(MIN_DELAY))
            .then(success => this._loadCredentialsComplete(success));
    }

    _loadCredentialsComplete(response) {
        //this.credentials = response;
        this.renderStep({
            stateId: STATE.STEP_CHOOSE_CREDENTIAL,
            stepElement: <PerforceCredentialsStep/>,
            afterStateId: null,
        });
    }

    selectCredential(credential) {
        this.selectedCred = credential;
        this._renderLoadingProjects();
    }

    _renderLoadingProjects() {
        this.renderStep({
            stateId: STATE.PENDING_LOADING_PROJECTS,
            stepElement: <PerforceLoadingStep/>,
            afterStateId: this._getProjectsStepAfterStateId(),
        });

        this.listProjects();
    }

    _getProjectsStepAfterStateId() {
        return STATE.STEP_CHOOSE_CREDENTIAL;
    }

    @action
    listProjects() {
        this._creationApi
            .listProjects(this.selectedCred)
            .then(waitAtLeast(MIN_DELAY))
            .then(projects => this._listProjectsSuccess(projects));
    }

    @action
    _listProjectsSuccess(response) {
        if (response.outcome === ListProjectsOutcome.SUCCESS) {
            this.projects = response.projects;
            this._renderChooseProject();
        } else if (response.outcome === ListProjectsOutcome.INVALID_CREDENTIAL_ID) {
            this.projects = response.projects;

            this.renderStep({
                stateId: STATE.STEP_CHOOSE_CREDENTIAL,
                stepElement: <PerforceCredentialsStep/>,
            });
        } else {
            this.renderStep({
                stateId: STATE.ERROR_UNKNOWN,
                stepElement: <PerforceUnknownErrorStep message={response.error}/>,
            });
        }
    }

    _renderChooseProject() {
        this.renderStep({
            stateId: STATE.STEP_CHOOSE_PROJECT,
            stepElement: <PerforceProjectListStep/>,
            afterStateId: this._getProjectsStepAfterStateId(),
        });
    }

    @action
    selectProject(project) {
        this.selectedProject = project;
        this.pipelineName = project;
        console.log("project and selectedProject: " + project + "::" + this.selectedProject);
    }

    saveRepo() {
        /*
        return this.credManager
            .findExistingCredential()
            .then(waitAtLeast(MIN_DELAY))
            .then(success => this._loadCredentialsComplete(success));
         */
        /*console.log("Reached PerforceFlowManager.saveRepo(). Building project " + this.selectedProject);
        const available = this._creationApi.checkPipelineNameAvailable(this.selectedProject);*/

        /*
        Check if the pipeline already exists
         */


        //console.log("PerforceCreationApi.saveRepo.available: " + available.outcome);

        this._saveRepo();
    }

    @action
    _saveRepo() {
        const afterStateId = this.isStateAdded(STATE.STEP_RENAME) ? STATE.STEP_RENAME : STATE.STEP_CHOOSE_PROJECT;

        this.renderStep({
            stateId: STATE.PENDING_CREATION_SAVING,
            stepElement: <PerforceCompleteStep/>,
            afterStateId,
        });

        this.setPlaceholders();

        this._initListeners();

        this._creationApi.createMbp(this.selectedCred, this.selectedProject, this.pipelineName)
            .then(waitAtLeast(MIN_DELAY * 2))
            .then(result => this._createPipelineComplete(result));
    }

    saveRenamedPipeline(pipelineName) {
        this.pipelineName = pipelineName;
        return this._saveRepo();
    }

    @action
    _createPipelineComplete(result) {
        console.log("PerforceFlowManager._createPipelineComplete.result.outcome: " + result.outcome);
        this.outcome = result.outcome;
        if (result.outcome === CreateMbpOutcome.SUCCESS) {
            this.changeState(STATE.STEP_COMPLETE_SUCCESS);
            this.pipeline = this.pipelineName;

            /*if (!this.isStateAdded(STATE.STEP_COMPLETE_MISSING_JENKINSFILE)) {
                this._checkForBranchCreation(
                    result.pipeline,
                    true,
                    ({isFound, hasError, pipeline}) => {
                        if (!hasError && isFound) {
                            this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
                            this.pipeline = pipeline;
                            this.pipelineName = pipeline.pipeline.name;
                        }
                    },
                    this.redirectTimeout
                );
                if (!this.isStateAdded(STATE.STEP_COMPLETE_MISSING_JENKINSFILE) && !this.isStateAdded(STATE.STEP_COMPLETE_SUCCESS)) {
                    this.changeState(STATE.PENDING_CREATION_EVENTS);
                    this.pipeline = result.pipeline;
                    this.pipelineName = result.pipeline;
                }
            }*/
        } else if (result.outcome === CreateMbpOutcome.INVALID_NAME) {
            console.log("PerforceFlowManager._createPipelineComplete, invalid name ");
            this.renderStep({
                stateId: STATE.STEP_RENAME,
                stepElement: <PerforceRenameStep pipelineName={this.pipelineName}/>,
                afterStateId: STATE.STEP_CHOOSE_PROJECT,
            });
            //this._showPlaceholder();
        } else if (result.outcome === CreateMbpOutcome.INVALID_URI || result.outcome === CreateMbpOutcome.INVALID_CREDENTIAL) {
            this.removeSteps({afterStateId: STATE.STEP_CREDENTIAL});
            this._showPlaceholder();
        } else if (result.outcome === CreateMbpOutcome.ERROR) {
            const afterStateId = this.isStateAdded(STATE.STEP_RENAME) ? STATE.STEP_RENAME : STATE.STEP_CHOOSE_REPOSITORY;
            this.renderStep({
                stateId: STATE.ERROR,
                stepElement: <PerforceUnknownErrorStep error={result.error}/>,
                afterStateId,
            });
        }
    }

    _checkForBranchCreation(pipelineName, multiBranchIndexingComplete, onComplete, delay = 500) {
        if (multiBranchIndexingComplete) {
            LOGGER.debug(`multibranch indexing for ${pipelineName} completed`);
        }

        LOGGER.debug(`will check for branches of ${pipelineName} in ${delay}ms`);

        //TODO change the this.selectedProject to actual pipeline name
        setTimeout(() => {
            this._creationApi.findBranches(this.selectedProject).then(data => {
                LOGGER.debug(`check for pipeline complete. created? ${data.isFound}`);
                onComplete(data);
            });
        }, delay);
    }

    checkPipelineNameAvailable(name) {
        if (!name) {
            return new Promise(resolve => resolve(false));
        }

        return this._creationApi.checkPipelineNameAvailable(name);
    }

    @action
    setPlaceholders(placeholders) {
        let array = [];

        if (typeof placeholders === 'string') {
            array.push(placeholders);
        } else if (placeholders) {
            array = placeholders;
        }

        this.placeholders.replace(array);
    }

    _initListeners() {
        this._cleanupListeners();

        LOGGER.info('P4: listening for project folder and multi-branch indexing events...');

        //TODO Add code here later
    }

    _cleanupListeners() {
        //TODO Add cleanup code here
    }

    _finishListening(stateId) {
        console.log('finishListening', stateId);
        this.changeState(stateId);
        this._cleanupListeners();
    }

}

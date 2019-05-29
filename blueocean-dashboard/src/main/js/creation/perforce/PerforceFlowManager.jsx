import React from 'react';
import {action, computed, observable} from 'mobx';

import {i18nTranslator, logging, sseService} from '@jenkins-cd/blueocean-core-js';

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
const SSE_TIMEOUT_DELAY = 1000 * 60;

/**
 * Impl of FlowManager for perforce creation flow.
 */
export default class PerforceFlowManager extends FlowManager {

    selectedCred = null;
    pipelineName = null;
    pipeline = null;

    _sseSubscribeId = null;
    _sseTimeoutId = null;

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


    _loadCredentialsList() {
        return this.credManager
            .findExistingCredential()
            .then(waitAtLeast(MIN_DELAY))
            .then(success => this._loadCredentialsComplete(success));
    }

    //TODO response is not used at the moment. Will be used when handling bad credentials later.
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
    }

    saveRepo() {

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
        this.outcome = result.outcome;
        if (result.outcome === CreateMbpOutcome.SUCCESS) {
            //TODO Assumption here is Jenkinsfile is present. So not checking for it.
            this._checkForBranchCreation(
                result.pipeline.name,
                true,
                ({ isFound, hasError, pipeline }) => {
                    if (!hasError && isFound) {
                        this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
                        this.pipeline = pipeline;
                        this.pipelineName = pipeline.name;
                    }
                },
                this.redirectTimeout
            );
        } else if (result.outcome === CreateMbpOutcome.INVALID_NAME) {
            this.renderStep({
                stateId: STATE.STEP_RENAME,
                stepElement: <PerforceRenameStep pipelineName={this.pipelineName}/>,
                afterStateId: STATE.STEP_CHOOSE_PROJECT,
            });
            this._showPlaceholder();
        } else if (result.outcome === CreateMbpOutcome.INVALID_URI || result.outcome === CreateMbpOutcome.INVALID_CREDENTIAL) {
            this.removeSteps({afterStateId: STATE.STEP_CREDENTIAL});
            this._showPlaceholder();
        } else if (result.outcome === CreateMbpOutcome.ERROR) {
            const afterStateId = this.isStateAdded(STATE.STEP_RENAME) ? STATE.STEP_RENAME : STATE.STEP_CHOOSE_PROJECT;
            this.renderStep({
                stateId: STATE.ERROR,
                stepElement: <PerforceUnknownErrorStep error={result.error}/>,
                afterStateId,
            });
        }
    }

    _showPlaceholder() {
        this.setPlaceholders([
            this.translate('creation.core.status.completed'),
        ]);
    }

    _checkForBranchCreation(pipelineName, multiBranchIndexingComplete, onComplete, delay = 500) {
        if (multiBranchIndexingComplete) {
            LOGGER.debug(`multibranch indexing for ${pipelineName} completed`);
        }

        LOGGER.debug(`will check for branches of ${pipelineName} in ${delay}ms`);

        setTimeout(() => {
            this._creationApi.findBranches(pipelineName).then(data => {
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

        this._sseSubscribeId = sseService.registerHandler(event => this._onSseEvent(event));
        this._sseTimeoutId = setTimeout(() => {
            this._onSseTimeout();
        }, SSE_TIMEOUT_DELAY);
    }

    _cleanupListeners() {
        if (this._sseSubscribeId || this._sseTimeoutId) {
            LOGGER.debug('cleaning up existing SSE listeners');
        }

        if (this._sseSubscribeId) {
            sseService.removeHandler(this._sseSubscribeId);
            this._sseSubscribeId = null;
        }
        if (this._sseTimeoutId) {
            clearTimeout(this._sseTimeoutId);
            this._sseTimeoutId = null;
        }
    }

    _onSseTimeout() {
        LOGGER.debug(`wait for events timed out after ${SSE_TIMEOUT_DELAY}ms`);
        this.changeState(STATE.STEP_COMPLETE_EVENT_TIMEOUT);
        this._cleanupListeners();
    }

    _onSseEvent(event) {
        if (LOGGER.isDebugEnabled()) {
            this._logEvent(event);
        }

        if (
            event.blueocean_job_pipeline_name === this.pipelineName &&
            event.jenkins_object_type === 'org.jenkinsci.plugins.workflow.job.WorkflowRun' &&
            (event.job_run_status === 'ALLOCATED' ||
                event.job_run_status === 'RUNNING' ||
                event.job_run_status === 'SUCCESS' ||
                event.job_run_status === 'FAILURE')
        ) {
            // set pipeline details that are needed later on in PerforceCompleteStep.navigatePipeline()
            this.pipeline = { organization: event.jenkins_org, fullName: this.pipelineName };
            this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
            return;
        }

        const multiBranchIndexingComplete = event.job_multibranch_indexing_result === 'SUCCESS' && event.blueocean_job_pipeline_name === this.pipelineName;

        if (multiBranchIndexingComplete) {
            LOGGER.info(`creation succeeded for ${this.pipelineName}`);
            if (event.jenkinsfile_present === 'false') {
                this._finishListening(STATE.STEP_COMPLETE_MISSING_JENKINSFILE);
            }
        } else if (event.job_multibranch_indexing_result === 'FAILURE') {
            this._finishListening(STATE.STEP_COMPLETE_EVENT_ERROR);
        } else {
            this._checkForBranchCreation(event.blueocean_job_pipeline_name, false, ({ isFound, hasError, pipeline }) => {
                if (isFound && !hasError) {
                    this._finishListening(STATE.STEP_COMPLETE_SUCCESS);
                    this.pipeline = pipeline;
                    this.pipelineName = pipeline.name;
                }
            });
        }
    }

    _finishListening(stateId) {
        console.log('PerforceFlowManager: finishListening()', stateId);
        this.changeState(stateId);
        this._cleanupListeners();
    }

}

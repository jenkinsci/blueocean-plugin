import { Enum } from '../flow2/Enum';

/**
 * Valid stateId's for PerforceFlowManager.
 */
const PerforceCreationState = new Enum({
    PENDING_LOADING_CREDS: 'pending_loading_creds',
    PENDING_LOADING_PROJECTS: 'pending_loading_projects',
    STEP_CHOOSE_CREDENTIAL: 'step_choose_credential',
    STEP_CHOOSE_PROJECT: 'step_choose_project',
    PENDING_CREATION_SAVING: 'pending_creation_saving',
    STEP_COMPLETE_SAVING_ERROR: 'step_complete_saving_error',
    STEP_COMPLETE_MISSING_JENKINSFILE: 'step_complete_missing_jenkinsfile',
    STEP_COMPLETE_SUCCESS: 'step_complete_success',
    STEP_RENAME: 'step_rename',
    ERROR_UNKNOWN: 'error_unknown',
});

export default PerforceCreationState;

import { Enum } from '../flow2/Enum';

/**
 * Valid stateId's for GitFlowManager.
 */
const GitCreationState = new Enum({
    LOADING_CREDENTIALS: 'loading_credentials',
    STEP_CONNECT: 'step_connect',
    CREATE_PIPELINE: 'create_pipeline',
    STEP_RENAME: 'step_rename',
    STEP_COMPLETE_SUCCESS: 'step_complete_success',
    STEP_COMPLETE_EVENT_ERROR: 'step_complete_event_error',
    STEP_COMPLETE_MISSING_JENKINSFILE: 'step_complete_missing_jenkinsfile',
    ERROR: 'error',
});

export default GitCreationState;

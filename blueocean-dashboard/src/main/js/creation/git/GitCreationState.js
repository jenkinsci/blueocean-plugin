import { Enum } from '../flow2/Enum';

/**
 * Valid stateId's for GitFlowManager.
 */
const GitCreationState = new Enum({
    LOADING_CREDENTIALS: 'loading_credentials',
    STEP_CONNECT: 'step_connect',
    CREATE_CREDS: 'create_creds',
    CREATE_PIPELINE: 'create_pipeline',
    STEP_RENAME: 'step_rename',
    COMPLETE: 'complete',
});

export default GitCreationState;

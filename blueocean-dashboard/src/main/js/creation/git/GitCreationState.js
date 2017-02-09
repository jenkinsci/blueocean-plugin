/**
 * Valid statuses for GitFlowManager.
 */
const state = {
    STEP_CONNECT: 'step_connect',
    CREATE_CREDS: 'create_creds',
    CREATE_PIPELINE: 'create_pipeline',
    STEP_RENAME: 'step_rename',
    COMPLETE: 'complete',

    values: () => [
        state.STEP_CONNECT,
        state.CREATE_CREDS,
        state.CREATE_PIPELINE,
        state.STEP_RENAME,
        state.COMPLETE,
    ],
};

export default state;

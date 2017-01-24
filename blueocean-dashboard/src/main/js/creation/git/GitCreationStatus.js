/**
 * Valid statuses for GitFlowManager.
 */
const status = {
    STEP_CONNECT: 'step_connect',
    CREATE_CREDS: 'create_creds',
    CREATE_PIPELINE: 'create_pipeline',
    STEP_RENAME: 'step_rename',
    COMPLETE: 'complete',

    values: () => [
        status.STEP_CONNECT,
        status.CREATE_CREDS,
        status.CREATE_PIPELINE,
        status.STEP_RENAME,
        status.COMPLETE,
    ],
};

export default status;

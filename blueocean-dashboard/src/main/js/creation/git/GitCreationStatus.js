/**
 * Created by cmeyers on 10/24/16.
 */

/**
 * Valid statuses for CompletedStep.
 */
const status = {
    NOT_STARTED: 'not_started',
    CREATE_CREDS: 'create_creds',
    CREATE_PIPELINE: 'create_pipeline',
    RUN_PIPELINE: 'run_pipeline',
    COMPLETE: 'complete',

    values: () => [status.NOT_STARTED, status.CREATE_CREDS, status.CREATE_PIPELINE, status.RUN_PIPELINE, status.COMPLETE],
};

export default status;

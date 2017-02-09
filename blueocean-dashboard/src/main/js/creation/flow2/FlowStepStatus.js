/**
 * Valid statuses for a FlowStep.
 */
const status = {
    ACTIVE: 'active',
    COMPLETE: 'complete',
    INCOMPLETE: 'incomplete',
    ERROR: 'error',

    values: () => [
        status.ACTIVE,
        status.COMPLETE,
        status.INCOMPLETE,
        status.ERROR,
    ],
};

export default status;

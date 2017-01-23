/**
 * Valid statuses for a FlowStep.
 */
const status = {
    ACTIVE: 'active',
    COMPLETE: 'complete',
    INCOMPLETE: 'incomplete',

    values: () => [status.ACTIVE, status.COMPLETE, status.INCOMPLETE],
};

export default status;

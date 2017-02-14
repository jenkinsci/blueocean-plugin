import { Enum } from './Enum';

/**
 * Valid statuses for a FlowStep.
 */
const FlowStepStatus = new Enum({
    ACTIVE: 'active',
    COMPLETE: 'complete',
    INCOMPLETE: 'incomplete',
    ERROR: 'error',
});

export default FlowStepStatus;

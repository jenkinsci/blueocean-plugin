import BbCloudCreationState from '../cloud/BbCloudCreationState';

/**
 * States for Bitbucket Server pipeline creation flow
 */
const BbServerCreationState = BbCloudCreationState.extend({
    PENDING_LOADING_SERVERS: 'PENDING_LOADING_SERVERS',
    STEP_CHOOSE_SERVER: 'STEP_CHOOSE_SERVER',
});

export default BbServerCreationState;

import { Enum } from '../../creation/flow2/Enum';

/**
 * Valid stateId's for BitBucketCredentialsStep
 */
const BbCredentialState = new Enum({
    PENDING_LOADING_CREDS: 'PENDING_LOADING_CREDS',
    NEW_REQUIRED: 'new_required',
    SAVE_SUCCESS: 'save_success',
    INVALID_CREDENTIAL: 'invalid_credential',
    REVOKED_CREDENTIAL: 'revoked_credential',
    UNEXPECTED_ERROR_CREDENTIAL: 'unexpected_error_credential',
});

export default BbCredentialState;

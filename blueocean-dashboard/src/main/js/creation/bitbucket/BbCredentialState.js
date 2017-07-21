import { Enum } from '../flow2/Enum';

/**
 * Valid stateId's for BitBucketCredentialsStep
 */
export const BbCredentialState = new Enum({
    NEW_REQUIRED: 'new_required',
    SAVE_SUCCESS: 'save_success',
    INVALID_CREDENTIAL: 'invalid_credential',
    UNEXPECTED_ERROR_CREDENTIAL: 'unexpected_error_credential',
});

import { Enum } from '../flow2/Enum';

/**
 * Valid stateId's for GithubCredentialsStep
 */
export const BbCredentialState = new Enum({
    NEW_REQUIRED: 'new_required',
    SAVE_SUCCESS: 'save_success',
});

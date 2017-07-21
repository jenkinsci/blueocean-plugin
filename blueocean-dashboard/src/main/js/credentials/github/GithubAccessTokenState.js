import { Enum } from '../../creation/flow2/Enum';

/**
 * Valid stateId's for GithubCredentialsStep
 */
const GithubAccessTokenState = new Enum({
    PENDING_LOADING_CREDS: 'PENDING_LOADING_CREDS',
    NEW_REQUIRED: 'new_required',
    EXISTING_REVOKED: 'existing_revoked',
    EXISTING_MISSING_SCOPES: 'existing_missing_scopes',
    VALIDATION_FAILED_TOKEN: 'validation_failed_token',
    VALIDATION_FAILED_SCOPES: 'validation_failed_scopes',
    ERROR_UNKNOWN: 'ERROR_UNKNOWN',
    SAVE_SUCCESS: 'save_success',
});

export default GithubAccessTokenState;

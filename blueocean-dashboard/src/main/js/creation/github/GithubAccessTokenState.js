import { Enum } from '../flow2/Enum';

/**
 * Valid stateId's for GithubCredentialsStep
 */
export const GithubAccessTokenState = new Enum({
    INITIAL: 'initial',
    NEW_REQUIRED: 'new_required',
    EXISTING_REVOKED: 'existing_revoked',
    EXISTING_MISSING_SCOPES: 'existing_missing_scopes',
    EXISTING_WAS_FOUND: 'existing_was_found',
    EXISTING_NOT_FOUND: 'existing_not_found',
    VALIDATION_FAILED_TOKEN: 'validation_failed_token',
    VALIDATION_FAILED_SCOPES: 'validation_failed_scopes',
    VALIDATION_FAILED_API_URL: 'validation_failed_api_url',
    VALIDATION_FAILED_UNKNOWN: 'validation_failed_unknown',
    SAVE_SUCCESS: 'save_success',
});

import { Enum } from '../../creation/flow2/Enum';

/**
 * Valid stateId's for GithubCredentialsManager/Picker
 */
const PerforceCredentialsState = new Enum({
    NEW_REQUIRED: 'new_required',
    EXISTING_REVOKED: 'existing_revoked',

});

export default PerforceCredentialsState;

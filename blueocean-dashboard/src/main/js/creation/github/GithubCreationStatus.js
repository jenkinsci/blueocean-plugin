/**
 * Valid statuses for GithubFlowManager.
 */
const status = {
    PENDING_LOADING_CREDS: 'pending_loading_creds',
    STEP_ACCESS_TOKEN: 'step_access_token',
    PENDING_VALIDATE_ACCESS_TOKEN: 'pending_validate_access_token',
    PENDING_LOADING_ORGANIZATIONS: 'pending_loading_organizations',
    STEP_CHOOSE_ORGANIZATION: 'step_choose_organization',
    STEP_CHOOSE_DISCOVER: 'step_choose_discover',
    STEP_ALREADY_DISCOVER: 'step_already_discover',
    PENDING_LOADING_REPOSITORIES: 'pending_loading_repositories',
    STEP_CONFIRM_DISCOVER: 'step_confirm_discover',
    STEP_CHOOSE_REPOSITORY: 'step_choose_repository',
    PENDING_CREATION: 'pending_creation',
    STEP_COMPLETE: 'step_complete',

    values: () => [
        status.PENDING_LOADING_CREDS,
        status.STEP_ACCESS_TOKEN,
        status.PENDING_VALIDATE_ACCESS_TOKEN,
        status.PENDING_LOADING_ORGANIZATIONS,
        status.STEP_CHOOSE_ORGANIZATION,
        status.STEP_CHOOSE_DISCOVER,
        status.STEP_ALREADY_DISCOVER,
        status.PENDING_LOADING_REPOSITORIES,
        status.PENDING_CREATION,
        status.STEP_CONFIRM_DISCOVER,
        status.STEP_CHOOSE_REPOSITORY,
        status.STEP_COMPLETE,
    ],
};

export default status;

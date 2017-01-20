/**
 * Valid statuses for GithubFlowManager.
 */
const status = {
    PENDING_LOADING_CREDS: 'pending_loading_creds',
    STEP_ACCESS_TOKEN: 'step_access_token',
    PENDING_VALIDATE_ACCESS_TOKEN: 'pending_validate_access_token',
    PENDING_LOADING_ORGANIZATIONS: 'pending_loading_organizations',
    STEP_CHOOSE_ORGANIZATION: 'step_choose_organization',
    PENDING_LOADING_REPOSITORIES: 'pending_loading_repositories',
    STEP_CONFIRM_AUTODISCOVER: 'step_confirm_autodiscover',
    STEP_CHOOSE_REPOSITORY: 'step_choose_repository',
    PENDING_CREATION: 'pending_creation',
    STEP_COMPLETE: 'step_complete',

    values: () => [
        status.PENDING_LOADING_CREDS,
        status.STEP_ACCESS_TOKEN,
        status.PENDING_VALIDATE_ACCESS_TOKEN,
        status.PENDING_LOADING_ORGANIZATIONS,
        status.STEP_CHOOSE_ORGANIZATION,
        status.PENDING_LOADING_REPOSITORIES,
        status.STEP_CONFIRM_AUTODISCOVER,
        status.PENDING_CREATION,
        status.STEP_COMPLETE,
    ],
};

export default status;

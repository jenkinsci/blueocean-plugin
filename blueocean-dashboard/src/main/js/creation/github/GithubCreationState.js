/**
 * Valid statuses for GithubFlowManager.
 */
const state = {
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
    PENDING_CREATION_SAVING: 'pending_creation_saving',
    STEP_COMPLETE_SAVING_ERROR: 'step_complete_saving_error',
    PENDING_CREATION_EVENTS: 'pending_creation_events',
    STEP_COMPLETE_EVENT_ERROR: 'step_complete_event_error',
    STEP_COMPLETE_EVENT_TIMEOUT: 'step_complete_event_timeout',
    STEP_COMPLETE_SUCCESS: 'step_complete_success',

    values: () => [
        state.PENDING_LOADING_CREDS,
        state.STEP_ACCESS_TOKEN,
        state.PENDING_VALIDATE_ACCESS_TOKEN,
        state.PENDING_LOADING_ORGANIZATIONS,
        state.STEP_CHOOSE_ORGANIZATION,
        state.STEP_CHOOSE_DISCOVER,
        state.STEP_ALREADY_DISCOVER,
        state.PENDING_LOADING_REPOSITORIES,
        state.STEP_CONFIRM_DISCOVER,
        state.STEP_CHOOSE_REPOSITORY,
        state.PENDING_CREATION_SAVING,
        state.STEP_COMPLETE_SAVING_ERROR,
        state.PENDING_CREATION_EVENTS,
        state.STEP_COMPLETE_EVENT_ERROR,
        state.STEP_COMPLETE_EVENT_TIMEOUT,
        state.STEP_COMPLETE_SUCCESS,
    ],
};

export default state;

import GithubCreationState from '../github/GithubCreationState';

const GHECreationState = GithubCreationState.extend({
    PENDING_LOADING_SERVERS: 'PENDING_LOADING_SERVERS',
    STEP_CHOOSE_SERVER: 'STEP_CHOOSE_SERVER',
});

export default GHECreationState;

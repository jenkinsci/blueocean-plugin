import Immutable from 'immutable';
/* We cannot extend Record
since we would return a function, */
/* eslint new-cap: [0] */
const { Record } = Immutable;
export const PipelineRecord = Record({
    displayName: '',
    name: '',
    organization: '',
    weatherScore: 0,
    branchNames: null,
    numberOfFailingBranches: 0,
    numberOfFailingPullRequests: 0,
    numberOfSuccessfulBranches: 0,
    numberOfSuccessfulPullRequests: 0,
    totalNumberOfBranches: 0,
    totalNumberOfPullRequests: 0,
});

export const ChangeSetRecord = Record({
    author: {
        email: null,
        fullName: null,
        id: null,
    },
    affectedPaths: null,
    commitId: null,
    comment: null,
    date: null,
    id: null,
    msg: null,
    paths: null,
    timestamp: null,
});

export const ActivityRecord = Record({
    changeSet: ChangeSetRecord,
    durationInMillis: null,
    enQueueTime: null,
    endTime: null,
    id: null,
    organization: null,
    pipeline: null,
    result: null,
    runSummary: null,
    startTime: null,
    state: null,
    type: null,
    commitId: null,
});

export const PullRequestRecord = Record({
    pullRequest: {
        author: null,
        id: null,
        title: null,
        url: null,
    },
});

export const RunsRecord = Record({
    latestRun: ActivityRecord,
    name: null,
    weatherScore: 0,
    pullRequest: PullRequestRecord,
});

export const State = Record({
    isFetching: false,
    pipelines: null,
    pipeline: PipelineRecord,
    runs: null,
    currentRuns: null,
    branches: null,
    currentBranches: null,
});

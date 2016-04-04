import Immutable from 'immutable';

export const PipelineRecord = Immutable.Record({ // eslint-disable-line new-cap
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

export const ActivityRecord = Immutable.Record({// eslint-disable-line
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

export const ChangeSetRecord = Immutable.Record({// eslint-disable-line
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

export const RunsRecord = Immutable.Record({
    latestRun: ActivityRecord,
    name: null,
    weatherScore: 0,
    pullRequest: PullRequestRecord,
}
);

export const PullRequestRecord = Immutable.Record({
    pullRequest: {
        author: null,
        id: null,
        title: null,
        url: null,
    }
});

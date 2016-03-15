import Immutable from 'immutable';

export const pipelineRecord = Immutable.Record({ // eslint-disable-line new-cap
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

export const activityRecord = Immutable.Record({// eslint-disable-line
    changeSet: changeSetRecord,
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

export const changeSetRecord = Immutable.Record({// eslint-disable-line
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

export const runsRecords = Immutable.Record({
    latestRun: activityRecord,
    name: null,
    weatherScore: 0,
    pullRequest: prRecords,
  }
);

export const prRecords = Immutable.Record({
  pullRequest: {
        author: null,
        id: null,
        title: null,
        url: null,
    }
});

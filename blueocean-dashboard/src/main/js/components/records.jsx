import Immutable from 'immutable';
/* We cannot extend Record
since we would return a function, */
/* eslint new-cap: [0] */
const { Record } = Immutable;
export class PipelineRecord extends Record({
    _class: null,
    _capabilities: [],
    _links: null,
    branchNames: null,
    displayName: '',
    estimatedDurationInMillis: 0,
    fullName: '',
    latestRun: null,
    name: '',
    numberOfFailingBranches: 0,
    numberOfFailingPullRequests: 0,
    numberOfFolders: null,
    numberOfPipelines: null,
    numberOfSuccessfulBranches: 0,
    numberOfSuccessfulPullRequests: 0,
    organization: '',
    totalNumberOfBranches: 0,
    totalNumberOfPullRequests: 0,
    weatherScore: 0,
}) {
    isFolder() {
        return this._class === 'io.jenkins.blueocean.service.embedded.rest.PipelineFolderImpl';
    }
}

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

export const PullRequestRecord = Record({
    pullRequest: {
        author: null,
        id: null,
        title: null,
        url: null,
    },
});

export const TestSummaryRecord = Record({
    testSummary: {
        failed: null,
        regressions: null,
        existingFailed: null,
        passed: null,
        fixed: null,
        skipped: null,
        total: null,
    },
});

export const branchDetailsRecord = Record({
    branch: {
        url: null,
        isPrimary: false,
    },
});

export class RunRecord extends Record({
    _class: null,
    _capabilities: [],
    _links: null,
    changeSet: [],
    causes: [],
    causeOfBlockage: null,
    artifacts: null,
    durationInMillis: null,
    enQueueTime: null,
    endTime: null,
    estimatedDurationInMillis: null,
    name: null,
    description: null,
    id: null,
    organization: null,
    pipeline: null,
    result: null,
    runSummary: null,
    startTime: null,
    state: null,
    type: null,
    commitId: null,
    parameters: null,
    artifactsZipFile: null,
    pullRequest: null,
    testSummary: TestSummaryRecord,
    replayable: null,
    branch: branchDetailsRecord,
}) {
    isQueued() {
        return this.state === 'QUEUED';
    }

    // We have a result
    isCompleted() {
        return this.result !== 'UNKNOWN';
    }

    isRunning() {
        return this.state === 'RUNNING';
    }

    isPaused() {
        return this.state === 'PAUSED';
    }

    getComputedResult() {
        if (this.isCompleted()) {
            return this.result;
        }
        return this.state;
    }
}

export const RunsRecord = Record({
    _class: null,
    _capabilities: [],
    _links: null,
    latestRun: RunRecord,
    parameters: null,
    name: null,
    weatherScore: 0,
    pullRequest: PullRequestRecord,
    permissions: {},
});

export const State = Record({
    isFetching: false,
    node: null,
    nodes: null,
    logs: null,
    messages: null,
    allPipelines: null,
    organizationPipelines: null,
    pipeline: null,
    runs: null,
    currentRuns: null,
    currentRun: null,
    branches: null,
    pullRequests: null,
    steps: null,
    currentBranches: null,
    tests: null,
});

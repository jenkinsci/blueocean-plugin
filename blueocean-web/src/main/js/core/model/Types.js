// @flow
export type LinkObject = {
    [ id: string ] : { href: string}
}

export type ActivityModel = {
    organization: string,
    pipeline: string,
    _links: LinkObject,
    changeSet: Object,
    durationInMillis: number,
    estimatedDurationInMillis: number,
    id: string,
    result: string,
    state: string,
    startTime: string,
    endTime: string;
    commitId: string
}

export type PipelineModel = {
    name: string,
    fullName: string,
    organization: string,
    numberOfSuccessfulBranches: number,
    numberOfFailingBranches: number,
    numberOfSuccessfulPullRequests: number,
    numberOfFailingPullRequests: number,
    displayName: string,
    weatherScore: string,
    _links: LinkObject,
    _class: string,
    latestRun: ActivityModel
}

export type BranchModel = PipelineModel & {
    pullRequest: Object
}

// @flow
import type { ActivityModel } from './ActivityModel';
export type LinkObject = {
    [ id: string ] : { href: string}
}

export type PipelineModelType = {
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

export type BranchModelType = PipelineModelType & {
    pullRequest: Object
}

export type ActivityModelType = {
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

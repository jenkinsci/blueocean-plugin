/**
 Model interfaces

 TODO: Hand-written for now, needs to be generated from Java sources with docstrings copied over etc
 */

export namespace Model {
    export interface WithClass {
        _class: string;
    }

    // From io.jenkins.blueocean.rest.model.BlueRun
    export enum RunState {
        QUEUED = 'QUEUED',
        RUNNING = 'RUNNING',
        PAUSED = 'PAUSED',
        SKIPPED = 'SKIPPED',
        NOT_BUILT = 'NOT_BUILT',
        FINISHED = 'FINISHED',
    }

    // From io.jenkins.blueocean.rest.model.BlueRun
    export enum RunResult {
        SUCCESS = 'SUCCESS',
        UNSTABLE = 'UNSTABLE',
        FAILURE = 'FAILURE',
        NOT_BUILT = 'NOT_BUILT',
        UNKNOWN = 'UNKNOWN',
        ABORTED = 'ABORTED',
    }

    // From io.jenkins.blueocean.rest.hal.Link
    export interface Link {
        href: string;
    }

    // From io.jenkins.blueocean.rest.model.Resource
    export interface Resource {
        link: Link;
        _links: { [key: string]: Link };
    }

    // From io.jenkins.blueocean.rest.model.BlueActionProxy
    export interface Action extends WithClass {
        urlName: string;
    }

    // From io.jenkins.blueocean.rest.model.BluePipelineItem
    export interface PipelineItem extends Resource, WithClass {
        organization: string;
        name: string;
        displayName: string;
        fullName: string;
        fullDisplayName: string;
        actions: Array<Action>;
    }

    // From io.jenkins.blueocean.rest.model.BlueRunnableItem
    export interface RunnableItem extends PipelineItem {
        weatherScore: number;
        latestRun: Run | null;
        estimatedDurationInMillis: number;
        parameters: Array<any>;
    }

    // From io.jenkins.blueocean.rest.model.BlueContainerItem
    export interface ContainerItem extends PipelineItem {
        numberOfFolders: number;
        numberOfPipelines: number;
        icon?: Resource;
        pipelineFolderNames: Array<string>;
    }

    // From io.jenkins.blueocean.rest.model.BlueMultiBranchItem
    export interface MultiBranchItem extends ContainerItem {
        totalNumberOfBranches: number;
        numberOfFailingBranches: number;
        numberOfSuccessfulBranches: number;
        totalNumberOfPullRequests: number;
        numberOfFailingPullRequests: number;
        numberOfSuccessfulPullRequests: number;
        branchNames: Array<string>;
    }

    // From io.jenkins.blueocean.rest.model.BlueItemRun
    export interface Run extends Resource, WithClass {
        organization: string;
        id: string;
        pipeline: string;
        name: string;
        description: string;
        changeSet: Array<Change>;
        startTime: string;
        enQueueTime: string;
        endTime: string;
        durationInMillis: number;
        estimatedDurationInMillis: number;
        state: RunState;
        result: RunResult;
        runSummary: string;
        type: string;
        artifactsZipFile: string;
        actions: Array<Action>;
        testSummary?: TestSummary;
        causes: Array<RunCause>;
        causeOfBlockage: string;
        replayable: boolean;
    }

    // From io.jenkins.blueocean.rest.model.BlueChangeSetEntry
    export interface Change extends Resource, WithClass {
        author: User;
        commitId: string;
        timestamp: string;
        msg: string;
        affectedPaths: Array<string>;
        url: string;
        issues?: Array<Issue>;
    }

    // From io.jenkins.blueocean.rest.model.BlueTestSummary
    export interface TestSummary {
        total: number;
        skipped: number;
        failed: number;
        passed: number;
        fixed: number;
        existingFailed: number;
        regressions: number;
    }

    // No nice interface for this, and no impl fields marked @Exported
    export interface RunCause {
        shortDescription: string;
    }

    // From io.jenkins.blueocean.rest.model.BlueUser
    export interface User extends Resource, WithClass {
        id: string;
        fullName: string;
        email?: string;
        permission: UserPermission;
        avatar: string;
    }

    // From io.jenkins.blueocean.rest.model.BlueIssue
    export interface Issue {
        id: string;
        url: string;
    }

    // From: io.jenkins.blueocean.rest.model.BlueUserPermission
    export interface UserPermission {
        administrator: boolean;
        pipeline: { [key: string]: boolean };
        credential: { [key: string]: boolean };
    }
}

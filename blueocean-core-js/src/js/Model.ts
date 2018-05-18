/**
 Model interfaces

 TODO: Hand-written for now, needs to be generated from Java sources with docstrings copied over etc
 */

namespace Model {
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

    // From io.jenkins.blueocean.rest.Reachable
    export interface Reachable {
        link: Link;
    }

    // From io.jenkins.blueocean.rest.model.BlueActionProxy
    export interface Action extends WithClass {
        urlName: string;
    }

    // From io.jenkins.blueocean.rest.model.BluePipelineItem
    export interface PipelineItem extends Reachable, WithClass {
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
        icon?: ItemIcon;
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

    export type ItemIcon = any; // TODO: Figure this out

    export interface Run {
        // TODO: define
    }
}

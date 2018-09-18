export enum Result {
    success = 'success',
    failure = 'failure',
    running = 'running',
    queued = 'queued',
    paused = 'paused',
    unstable = 'unstable',
    aborted = 'aborted',
    not_built = 'not_built', // May be pending, or job was ended before this point
    skipped = 'skipped', // excluded via pipeline "when" clause
    unknown = 'unknown', // bad data or client code needs updating for new values
}

export function decodeResultValue(resultMaybe: any): Result {
    const lcase = String(resultMaybe).toLowerCase();
    for (const enumKey of Object.keys(Result)) {
        const enumValue = Result[enumKey as any];
        if (enumKey.toLowerCase() === lcase || enumValue.toLowerCase() === lcase) {
            return enumValue as Result;
        }
    }

    return Result.unknown;
}

export const MATRIOSKA_PATHS = false;

// Dimensions used for layout, px
export const defaultLayout = {
    nodeSpacingH: 120,
    parallelSpacingH: 120,
    nodeSpacingV: 70,
    nodeRadius: 12,
    terminalRadius: 7,
    curveRadius: 12,
    connectorStrokeWidth: 3.5,
    labelOffsetV: 20,
    smallLabelOffsetV: 15,
    ypStart: 55,
};

// Typedefs

// TODO: Change "export type Foo = {}" to "export interface Foo {}"

export type StageType = string; // TODO: STAGE, PARALLEL, STEP

/**
 * StageInfo is the input, in the form of an Array<StageInfo> of the top-level stages of a pipeline
 */
export interface StageInfo {
    name: string;
    title: string;
    state: Result;
    completePercent: number;
    id: number;
    type: StageType;
    children: Array<StageInfo>; // Used by the top-most stages with parallel branches
    nextSibling?: StageInfo; // Used within a parallel branch to denote sequential stages
    isSequential?: boolean;
}

// TODO: Refactor these into a common base, and some discerning "typeof" funcs

export interface BaseNodeInfo {
    key: string;
    x: number;
    y: number;
    id: number;
    name: string;

    // -- Marker
    isPlaceholder: boolean;
}

export interface StageNodeInfo extends BaseNodeInfo {
    // -- Marker
    isPlaceholder: false;

    // -- Unique
    stage: StageInfo;
}

export interface PlaceholderNodeInfo extends BaseNodeInfo {
    // -- Marker
    isPlaceholder: true;

    // -- Unique
    type: 'start' | 'end';
}

export type NodeInfo = StageNodeInfo | PlaceholderNodeInfo;

export interface NodeColumn {
    topStage?: StageInfo; // Top-most stage for this column, which will have no rendered nodes if it's parallel
    rows: Array<Array<NodeInfo>>;
    x: number; // Center X position, for positioning top bigLabel
}

export interface CompositeConnection {
    sourceNodes: Array<NodeInfo>;
    destinationNodes: Array<NodeInfo>;
    skippedNodes: Array<NodeInfo>;
}

export interface LabelInfo {
    x: number;
    y: number;
    text: string;
    key: string;
    stage?: StageInfo;
    node: NodeInfo;
}

export type LayoutInfo = typeof defaultLayout;

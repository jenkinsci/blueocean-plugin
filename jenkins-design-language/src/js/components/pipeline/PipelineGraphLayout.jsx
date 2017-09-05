// @flow

import React from 'react';

import type {
    NodeColumn,
    LabelInfo,
    LayoutInfo,
    StageInfo,
} from './PipelineGraphModel';

// TODO: Docs
export function layoutGraph(newStages: Array<StageInfo>, layout: LayoutInfo) {

    const stageNodeColumns = createNodeColumns(newStages);
    const { nodeSpacingH, ypStart } = layout;

    const startNode = {
        x: 0,
        y: 0,
        name: 'Start',
        id: -1,
        isPlaceholder: true,
        key: 'start-node',
        type: 'start',
    };

    const endNode = {
        x: 0,
        y: 0,
        name: 'End',
        id: -2,
        isPlaceholder: true,
        key: 'end-node',
        type: 'end',
    };

    const allNodeColumns = [
        { nodes: [startNode] },
        ...stageNodeColumns,
        { nodes: [endNode] },
    ];

    positionNodes(allNodeColumns, layout);

    const bigLabels = createBigLabels(allNodeColumns);
    const smallLabels = createSmallLabels(allNodeColumns);
    const connections = createConnections(allNodeColumns);

    // Calculate the size of the graph
    let measuredWidth = 0;
    let measuredHeight = 200;

    for (const column of allNodeColumns) {
        for (const node of column.nodes) {
            measuredWidth = Math.max(measuredWidth, node.x + nodeSpacingH / 2);
            measuredHeight = Math.max(measuredHeight, node.y + ypStart);
        }
    }

    // TODO: Type
    return {
        nodeColumns: allNodeColumns,
        connections,
        bigLabels,
        smallLabels,
        measuredWidth,
        measuredHeight,
    };
}

/**
 * Generate an array of columns, based on the top-level stages
 */
function createNodeColumns(topLevelStages: Array<StageInfo> = []): Array<NodeColumn> {

    const nodeColumns = [];

    for (const topStage of topLevelStages) {
        // If stage has children, we don't draw a node for it, just its children
        const stagesForColumn =
            topStage.children && topStage.children.length ? topStage.children : [topStage];

        const column = {
            topStage,
            nodes: [],
        };

        column.nodes = stagesForColumn
            .filter(nodeStage => !!nodeStage)
            .map(nodeStage => ({
                x: 0, // Layout is done later
                y: 0,
                name: nodeStage.name,
                id: nodeStage.id,
                stage: nodeStage,
                isPlaceholder: false,
                key: 'n_' + nodeStage.id,
            }));

        nodeColumns.push(column);
    }

    return nodeColumns;
}


/**
 * Walks the columns of nodes giving them x and y positions. Mutates the node objects in place for now.
 */
function positionNodes(nodeColumns: Array<NodeColumn>, { nodeSpacingH, nodeSpacingV, ypStart }) {

    let xp = nodeSpacingH / 2;
    let previousTopNode = null;

    for (const column of nodeColumns) {
        const topNode = column.nodes[0];

        let yp = ypStart; // Reset Y to top for each column

        if (previousTopNode) {
            // Advance X position
            if (previousTopNode.isPlaceholder || topNode.isPlaceholder) {
                // Don't space placeholder nodes (start/end) as wide as normal.
                xp += Math.floor(nodeSpacingH * 0.7);
            } else {
                xp += nodeSpacingH;
            }
        }

        for (const node of column.nodes) {
            node.x = xp;
            node.y = yp;

            yp += nodeSpacingV;
        }

        previousTopNode = topNode;
    }
}


/**
 * Generate label descriptions for big labels at the top of each column
 */
function createBigLabels(columns: Array<NodeColumn>) {

    const labels = [];

    for (const column of columns) {

        const node = column.nodes[0];
        const stage = column.topStage;
        const text = stage ? stage.name : node.name;
        const key = 'l_b_' + node.key;

        labels.push({
            x: node.x,
            y: node.y,
            node,
            stage,
            text,
            key,
        });
    }

    return labels;
}

/**
 * Generate label descriptions for small labels under the nodes
 */
function createSmallLabels(columns: Array<NodeColumn>) {

    const labels = [];

    for (const column of columns) {
        if (column.nodes.length === 1) {
            continue; // No small labels for single-node columns
        }
        for (const node of column.nodes) {
            const label: LabelInfo = {
                x: node.x,
                y: node.y,
                text: node.name,
                key: 'l_s_' + node.key,
                node,
            };

            if (node.isPlaceholder === false) {
                label.stage = node.stage;
            }

            labels.push(label);
        }
    }

    return labels;
}

/**
 * Generate connection information from column to column
 */
function createConnections(columns: Array<NodeColumn>) {

    const connections = [];

    let sourceNodes = [];
    let skippedNodes = [];

    for (const column of columns) {
        if (column.topStage && column.topStage.state === 'skipped') {
            skippedNodes.push(column.nodes[0]);
            continue;
        }

        if (sourceNodes.length) {
            connections.push({
                sourceNodes,
                destinationNodes: column.nodes,
                skippedNodes: skippedNodes,
            });
        }

        sourceNodes = column.nodes;
        skippedNodes = [];
    }

    return connections;
}
